import { environment } from '../../environments/environment';
import { RxStompService } from './rx-stomp.service';

export function rxStompServiceFactory() {
    const rxStomp = new RxStompService();
    rxStomp.configure({
        brokerURL: environment.messageBrokerURL,

        /*connectHeaders: {
            login: 'guest',
            passcode: 'guest',
        },*/

        heartbeatIncoming: 0,
        heartbeatOutgoing: 20000,

        reconnectDelay: 200,

        debug: (msg: string): void => {
            if(localStorage.getItem("isDebug"))
                console.log(new Date(), msg);
        },
    });
    rxStomp.activate();
    return rxStomp;
}