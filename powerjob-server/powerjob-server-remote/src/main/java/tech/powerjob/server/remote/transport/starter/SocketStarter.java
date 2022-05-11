package tech.powerjob.server.remote.transport.starter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.BufferUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.hutool.socket.aio.AioServer;
import cn.hutool.socket.aio.AioSession;
import cn.hutool.socket.aio.SimpleIoAction;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author: zmx
 * @date 2022/5/11
 */
public class SocketStarter {

    public static void init() {
        AioServer aioServer = new AioServer(7707);
        aioServer.setIoAction(new SimpleIoAction() {

            @Override
            public void accept(AioSession session) {
                StaticLog.debug("【客户端】：{} 连接。", session.getRemoteAddress());
                session.write(BufferUtil.createUtf8("=== Welcome to Hutool socket server. ==="));
            }

            @Override
            public void doAction(AioSession session, ByteBuffer data) {
                String s = Charset.forName("utf-8").decode(data).toString();
                Console.log(s);
            }
        }).start(false);

    }
}
