package link.thingscloud.freeswitch.esl.spring.boot.starter.example.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import link.thingscloud.freeswitch.esl.InboundClient;
import link.thingscloud.freeswitch.esl.constant.EventNames;
import link.thingscloud.freeswitch.esl.helper.EslHelper;
import link.thingscloud.freeswitch.esl.spring.boot.starter.annotation.EslEventName;
import link.thingscloud.freeswitch.esl.spring.boot.starter.handler.EslEventHandler;
import link.thingscloud.freeswitch.esl.spring.boot.starter.propeties.OutboundClientProperties;
import link.thingscloud.freeswitch.esl.transport.CommandResponse;
import link.thingscloud.freeswitch.esl.transport.SendMsg;
import link.thingscloud.freeswitch.esl.transport.event.EslEvent;
import link.thingscloud.freeswitch.esl.transport.message.EslMessage;
import link.thingscloud.freeswitch.esl.util.EslEventUtil;
import link.thingscloud.spring.boot.common.aop.annotation.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * do: FS --> [Inbound] --> app --> [sendMsg] --> socket address
 * do: FS <--> [Outbound] <--> app
 *
 * @author th158
 */
@Slf4j
@EslEventName(EventNames.CHANNEL_CREATE)
@Component
public class InboundChannelCreateSendSocketHandler implements EslEventHandler {

    @NacosInjected
    private NamingService namingService;

    @Autowired
    private InboundClient inboundClient;

    @Autowired
    private OutboundClientProperties outboundClientProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    @RedisLock(lockName = EventNames.CHANNEL_CREATE, key = "coreUUID")
    public void handle(String address, EslEvent event, String coreUUID) {
        SendMsg sendMsg = new SendMsg(EslEventUtil.getCallerUniqueId(event));

        log.info("Inbound CHANNEL_CREATE:[{}] [{}]  [{}]", address, coreUUID, JSON.toJSONString(event));

        try {
            // ?????? ?????? ??? inbound ??????
            if ("inbound".equals(EslEventUtil.getCallerDirection(event))) {
                // ???????????????????????????????????????????????????????????????
                Instance instance = namingService.selectOneHealthyInstance("softswitch-gateway");
                // ???fs ?????? socket ??????
                sendMsg.addCallCommand("execute");
                sendMsg.addExecuteAppName("socket");
                // ??????  <action application="socket" data=" IP : yaml??????????????? async full" />
                String arg = instance.getIp() + ":" + outboundClientProperties.getServer().getPort() + " async full";

                log.info("instance socket: ip [{}] port [{}], arg: [{}]", instance.getIp(), outboundClientProperties.getServer().getPort(), arg);
                sendMsg.addExecuteAppArg(arg);
                //inboundClient.sendMessage(address, sendMsg);


//                String callerUUID = EslEventUtil.getCallChannelUuid(event);
//
//                EslMessage phoneHold = inboundClient.sendSyncApiCommand(address, "uuid_phone_event", callerUUID + " hold");
//                log.info("phoneHold: {}", JSON.toJSONString(phoneHold));
//
//                EslMessage eslMessageUUID = inboundClient.sendSyncApiCommand(address, "create_uuid","");
//                String calleeUUID =  eslMessageUUID.getBodyLines().get(0);
//                log.info("create_uuid:  {}", calleeUUID);
//
//                EslMessage originate =  inboundClient.sendSyncApiCommand(address, "originate", "{origination_uuid="+ calleeUUID +"}sofia/external/" + EslEventUtil.getSipToUri(event) + " &park");
//                log.info("originate: {}", JSON.toJSONString(originate));
//
//                EslMessage uuidBridge =  inboundClient.sendSyncApiCommand(address, "uuid_bridge", calleeUUID + " " + callerUUID);
//                log.info("uuidBridge: {}", JSON.toJSONString(uuidBridge));
//
//                EslMessage phoneTalk = inboundClient.sendSyncApiCommand(address, "uuid_phone_event", callerUUID + " talk");
//                log.info("phoneTalk: {}", JSON.toJSONString(phoneTalk));
//
//                // uuid_bridge

            }

            EslMessage eslMessage = inboundClient.sendSyncApiCommand(address, "show", "channels as json");
            log.info("show channels as json {}", JSON.toJSONString(eslMessage));

        } catch (NacosException e) {
            e.printStackTrace();
        }
        log.debug("address[{}] EslEvent[{}]", address, EslHelper.formatEslEvent(event));
    }


}
