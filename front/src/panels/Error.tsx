import {Button, Flex, Panel, Placeholder} from "@vkontakte/vkui";
import {Icon20ErrorCircleFillRed} from "@vkontakte/icons";
import {closeApp} from "../consts.ts";

export interface ErrorProps {
    message: string
}

export default function Error(props: ErrorProps) {
    return (
        <Panel centered>
            <Placeholder
                icon={<Icon20ErrorCircleFillRed width={64} height={64}/>}
                header="Ошибка"
                action={
                    <Flex gap="l" direction="column">
                        <Button size="l" appearance="negative" onClick={closeApp}>
                            Закрыть
                        </Button>
                    </Flex>

                }
            >
                <p>{props.message}</p>
            </Placeholder>
        </Panel>
    );
}