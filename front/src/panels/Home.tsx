import {FC, useState} from 'react';
import {
    Button,
    FormItem,
    Group,
    Image,
    Input,
    NavIdProps,
    Panel,
    PanelHeader,
    SimpleCell,
    Textarea
} from '@vkontakte/vkui';
import {VideoInfo, VideoSaveRequest, VK_API_VERSION} from "../consts.ts";
import axios, {AxiosError} from "axios";
import bridge, {GroupInfo, UserInfo} from "@vkontakte/vk-bridge";
import Success from "./Success.tsx";
import Error from "./Error.tsx";
import GroupSelect from "./GroupSelect.tsx";

const ENDPOINT = import.meta.env.VITE_APP_HOST + "/videos/save"

export interface HomeProps extends NavIdProps {
    id: string,

    user: UserInfo,
    video: VideoInfo
    token: string
}

export const Home: FC<HomeProps> = (props) => {
    const video = props.video;
    const [clicked, setClicked] = useState(false)
    const [completed, setCompleted] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [videoTitle, setVideoTitle] = useState(props.video.name)
    const [videoDescription, setVideoDescription] = useState("Загружено через @unlock_video")
    const [group, setGroup] = useState<GroupInfo | null>(null)

    async function save() {
        if (clicked) return
        setClicked(true)
        const response = await bridge.send("VKWebAppCallAPIMethod", {
            method: "video.save", params: {
                access_token: props.token,
                v: VK_API_VERSION,
                name: videoTitle,
                description: videoDescription,
                // @ts-expect-error я манал ещё проперти эту обрабатывать, мне тупо впадлу
                group_id: group?.id
            }
        })
        const url = response.response["upload_url"]
        const request: VideoSaveRequest = {
            id: props.id,
            userId: props.user.id,
            groupId: group?.id,
            uploadUrl: url,
        }
        try {
            await axios.post(ENDPOINT, request)
            setCompleted(true)
        } catch (e) {
            const error = e as AxiosError;
            let message: string
            if (error.response && error.response.status === 400)
                // @ts-expect-error я е**л этот ваш js
                message = error.response.data["message"] as string;
            else {
                console.log("Unknown error", error.response)
                message = "Неизвестная ошибка"
            }
            setError(message)
        }
    }

    function content() {
        if (error == null && !completed)
            return <form onSubmit={(e) => {
                e.preventDefault();
                save()
            }}>
                <SimpleCell before={<Image src={video.preview} borderRadius="l" size={64}/>}
                            subtitle={"https://vk.com/" + video.id}>
                    {video.name}
                </SimpleCell>
                <FormItem top="Название видео" required>
                    <Input value={videoTitle} onChange={e => setVideoTitle(e.target.value)}/>
                </FormItem>
                <FormItem top="Описание">
                    <Textarea value={videoDescription} onChange={e => setVideoDescription(e.target.value)}/>
                </FormItem>
                <GroupSelect user={props.user} token={props.token} setValue={setGroup}/>
                <FormItem>
                    <Button type="submit" size="l" stretched disabled={clicked}>
                        Сохранить
                    </Button>
                </FormItem>
            </form>
        else if (completed) return <Success/>
        else if (error) return <Error message={error}/>
        else return <template>хуй</template>
    }

    return (
        <Panel id={props.id} className="p-2">
            <PanelHeader>Сохранение видео</PanelHeader>
            <Group>
                {content()}
            </Group>
        </Panel>
    );
};
