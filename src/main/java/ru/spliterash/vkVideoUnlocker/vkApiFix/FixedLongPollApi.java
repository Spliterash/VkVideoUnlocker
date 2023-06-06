package ru.spliterash.vkVideoUnlocker.vkApiFix;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.events.EventsHandler;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.objects.callback.messages.CallbackMessage;
import com.vk.api.sdk.objects.groups.LongPollServer;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import org.apache.http.ConnectionClosedException;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class FixedLongPollApi extends EventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FixedLongPollApi.class);

    private final VkApiClient client;

    private final int waitTime;

    private volatile boolean isRunning = false;

    protected FixedLongPollApi(VkApiClient client, int waitTime) {
        this.client = client;
        this.waitTime = waitTime;
    }

    private LongPollServer initServer(GetLongPollServerResponse lpServerResponse) {
        return new LongPollServer()
                .setKey(lpServerResponse.getKey())
                .setTs(lpServerResponse.getTs())
                .setServer(lpServerResponse.getServer());
    }

    private LongPollServer getLongPollServer(UserActor actor, int groupId) {
        try {
            return initServer(client.groupsLongPoll().getLongPollServer(actor, groupId).execute());
        } catch (ApiException | ClientException e) {
            return null;
        }
    }

    private LongPollServer getLongPollServer(GroupActor actor) {
        try {
            return initServer(client.groupsLongPoll().getLongPollServer(actor, actor.getGroupId()).execute());
        } catch (ApiException | ClientException e) {
            return null;
        }
    }

    private void handleUpdates(LongPollServer lpServer) throws ConnectionClosedException {
        if (lpServer == null)
            LOG.error("Getting LongPoll server was failed");

        isRunning = true;
        try {
            LOG.info("LongPoll handler started to handle events");
            GetLongPollEventsResponse eventsResponse;
            String timestamp = lpServer.getTs();
            while (isRunning) {
                eventsResponse = client.longPoll()
                        .getEvents(lpServer.getServer(), lpServer.getKey(), timestamp)
                        .waitTime(waitTime)
                        .execute();
                eventsResponse.getUpdates().forEach(e -> parse(gson.fromJson(e, CallbackMessage.class)));
                timestamp = eventsResponse.getTs();
            }
            LOG.info("LongPoll handler stopped to handle events");
        } catch (ApiException | ClientException e) {
            /*
            Actually instead of GetLongPollEventsResponse there might be returned error like:
            {"failed":1,"ts":30} or {"failed":2}, but it directly handled in execute() method.
            There are 2 ways: deserialize manually response from string OR do reconnection in each
            error case. There is second way - keep use typed object and reconnect when any error.
            */
            LOG.error("Getting LongPoll events was failed", e);
            throw new ConnectionClosedException();
        }
        isRunning = false;
    }

    public void run(GroupActor actor) {
        Executors.newSingleThreadExecutor().execute(
                () -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            LongPollServer lpServer = getLongPollServer(actor);
                            if (lpServer == null) {
                                LOG.warn("Failed to get lp server, waiting 10 second");
                                Thread.sleep(10000);
                                continue;
                            }
                            handleUpdates(lpServer);
                        } catch (ConnectionClosedException ignored) {
                            // IGNORE
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
