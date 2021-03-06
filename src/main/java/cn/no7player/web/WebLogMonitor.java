package cn.no7player.web;

import cn.no7player.config.CommonConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.InputStream;

@ServerEndpoint("/webLogMonitor")
@Component
public class WebLogMonitor {
	private static Log logger = LogFactory.getLog(WebLogMonitor.class);

	private Process process;
	private InputStream inputStream;

	/**
	 * 新的WebSocket请求开启
	 */
	@OnOpen
	public void onOpen(Session session) {
		try {
			// 执行tail -f命令
			logger.info(""+CommonConfig.getLogpath());
			process = Runtime.getRuntime().exec("tail -f "+CommonConfig.getLogpath());
			inputStream = process.getInputStream();
			
			// 启动新的线程，防止InputStream阻塞处理WebSocket的线程
			WebLogThread thread = new WebLogThread(inputStream, session);
			thread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * WebSocket请求关闭
	 */
	@OnClose
	public void onClose() {
		try {
			if(inputStream != null)
				inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(process != null)
			process.destroy();
	}
	
	@OnError
	public void onError(Throwable thr) {
		thr.printStackTrace();
	}
}