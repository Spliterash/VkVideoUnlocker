import {Button, Flex, Panel, Placeholder} from "@vkontakte/vkui";
import {Icon20CheckCircleFillGreen} from "@vkontakte/icons";
import {closeApp} from "../consts.ts";


export default function Success() {
    return (
        <Panel centered>
            <Placeholder
                icon={<Icon20CheckCircleFillGreen width={64} height={64}/>}
                header="Успех"
                action={
                    <Flex gap="l" direction="column">
                        <Button size="l" appearance="negative" onClick={closeApp}>
                            Закрыть
                        </Button>
                    </Flex>

                }
            >
                <p>Ссылка на загрузку видео успешно получена</p>
                <p>О результате отпишу ответным сообщением</p>
            </Placeholder>
        </Panel>
    );
}