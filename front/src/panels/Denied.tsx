import {Button, Flex, Panel, Placeholder} from "@vkontakte/vkui";
import {Icon16AddCircleFillRed} from "@vkontakte/icons";
import {closeApp} from "../consts.ts";

export interface AccessDeniedPageProps {
    onRetry: () => void;
}

export default function Denied(props: AccessDeniedPageProps) {
    return (
        <Panel centered>
            <Placeholder
                icon={<Icon16AddCircleFillRed width={64} height={64}/>}
                header="Жадина говядина"
                action={
                    <Flex gap="l" direction="column">
                        <Button size="l" onClick={props.onRetry}>
                            Ладно, так уж и быть, выдам
                        </Button>
                        <Button size="l" appearance="negative" onClick={closeApp}>
                            Не, я тебе не доверяю
                        </Button>
                    </Flex>

                }
            >
                <p>Как по твоему, я должен перезаливать твой видос, если ты мне не даёшь прав, гений?</p>
            </Placeholder>
        </Panel>
    );
}