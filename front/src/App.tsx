import {ReactNode, useEffect, useState} from 'react';
import {ScreenSpinner, SplitCol, SplitLayout} from '@vkontakte/vkui';

import {Home} from './panels';
import {APP_ID, HashInput} from "./consts.ts";
import Denied from "./panels/Denied.tsx";
import bridge, {ReceiveData, UserInfo} from "@vkontakte/vk-bridge";
import Error from "./panels/Error.tsx";
// Поебать
let hash: HashInput | null = null

try {
    hash = JSON.parse(decodeURI(window.location.hash.substring(1)))
} catch (e) {
    // ignore
}

export const App = () => {
    const [token, setToken] = useState<string | null>(null)
    const [popout, setPopout] = useState<ReactNode | null>(<ScreenSpinner size="large"/>);
    const [user, setUser] = useState<UserInfo | null>(null)
    const [denied, setDenied] = useState(false)

    async function fetchData() {
        await askPermissions()
        setUser(await bridge.send('VKWebAppGetUserInfo'))
        setPopout(null)
    }

    async function askPermissions() {
        let response: ReceiveData<"VKWebAppGetAuthToken">
        try {
            response = await bridge.send("VKWebAppGetAuthToken", {
                app_id: APP_ID,
                scope: "video"
            })
            if (response.scope != "video") {
                setDenied(true)

                return
            }
        } catch (e) {
            setDenied(true)
            return
        } finally {
            setPopout(null)
        }

        setDenied(false)
        setToken(response.access_token)
    }

    useEffect(() => {
        if (hash != null)
            fetchData();
        else
            setPopout(null)
    }, []);

    function component() {
        if (hash == null)
            return <Error message={"Прямое открытие приложения не поддерживается"}/>
        else if (denied)
            return <Denied onRetry={askPermissions}/>
        else if (popout == null && user != null)
            return <Home id={hash.id} token={token ?? ""} video={hash.video} user={user}/>
        else
            return <div/>
    }

    return (
        <SplitLayout popout={popout}>
            <SplitCol>
                {component()}
            </SplitCol>
        </SplitLayout>
    );
};
