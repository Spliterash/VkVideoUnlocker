import bridge, {GroupInfo, UserInfo} from "@vkontakte/vk-bridge";
import {VK_API_VERSION} from "../consts.ts";
import {ChangeEvent, useState} from "react";
import {Avatar, CustomSelectOption, FormItem, Select} from "@vkontakte/vkui";
import {CustomSelectOptionInterface} from "@vkontakte/vkui/src/components/CustomSelect/CustomSelect.tsx";


export interface GroupSelectProps {
    token: string,
    user: UserInfo,
    setValue: (current: GroupInfo | null) => void
}

const User: CustomSelectOptionInterface = {
    value: "user",
    label: "Свой аккаунт",
}

interface Group extends CustomSelectOptionInterface {
    group: GroupInfo
}

type Row = typeof User | Group

export default function GroupSelect({token, setValue, user}: GroupSelectProps) {
    const [nativeValue, setNativeValue] = useState<string>("user")
    const [listOptions, setListOptions] = useState<Row[]>([User])
    const [loading, setLoading] = useState(false)

    async function checkGroups() {
        if (loading) return
        if (listOptions.length > 1) return
        setLoading(true)
        try {
            const result = await bridge.send("VKWebAppCallAPIMethod", {
                method: "groups.get",
                params: {
                    access_token: token,
                    v: VK_API_VERSION,
                    filter: "admin,editor",
                    extended: 1
                }
            })
            const groups = result.response.items as GroupInfo[]
            const newList: Row[] = groups.map(function (g: GroupInfo): Group {
                return {
                    value: g.id,
                    label: g.name,
                    group: g
                }
            })
            newList.unshift(User)

            setListOptions(newList)
        } finally {
            setLoading(false)
        }
    }

    function handleChange(event: ChangeEvent<HTMLSelectElement>) {
        const value = event.target.value;
        setNativeValue(event.target.value)

        const select = listOptions.find(e => e.value == value)

        if (!select) return
        else if (select == User) setValue(null)
        else setValue(select.group)
    }

    function getAvatar(option: Row): string {
        if (option == User) return user.photo_100
        else return option.group.photo_100
    }

    return <FormItem
        top="Сохранить в"
        bottom="Выберите куда видео будет сохраненно"
        onClick={checkGroups}
    >
        <Select
            fetching={loading}
            value={nativeValue}
            options={listOptions}
            onOpen={checkGroups}
            onChange={handleChange}
            renderOption={({option, ...restProps}) => (
                <CustomSelectOption
                    {...restProps}
                    key={option.value as string}
                    before={<Avatar size={24} src={getAvatar(option)}/>}
                />)}
        />
    </FormItem>
}