package com.autotoll.gps.thread;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.autotoll.gps.check.Config;

public class CheckerThread {
	private static Logger logger = Logger.getLogger(CheckerThread.class);
	private static Object lock = new Object();
	private static AtomicBoolean isStopping = new AtomicBoolean(false);

	public static void check() {
		String gaterUrl = Config.getProperty("gater.url","");
		int unHandleMax = Config.getInt("gater.unhandle.max", 1000000);
		int onlineMin = Config.getInt("gater.online.min", 1);
		int checkInterval = Config.getInt("thread.check.interval", 30);
		int delay = Config.getInt("gater.restart.delay", 25);
		while (true) {
			try {
				if (isStopping.get()) {
					logger.info("等待网关重启");
					synchronized (lock) {
						lock.wait();
					}
				}
				isStopping.set(false);
				Document doc = Jsoup.connect(gaterUrl).get();
				logger.info("开始检测网关数据");
				Elements elements = doc.getElementsByTag("tr");
				Element element = elements.get(2);//第3行
				Element unHandleElement = element.children().get(2);//第3列;未处理数
				int unHandleNumber = Integer.parseInt(unHandleElement.text());//未处理数
				Element onlineElement = elements.get(6);//第7行
				Element onlineNumberElement = onlineElement.children().get(1);//第2列;在线终端数
				int onlineNumber = Integer.parseInt(onlineNumberElement.text());//在线终端数
				if(unHandleNumber >= unHandleMax || onlineNumber <onlineMin){
					StringBuilder builder = new StringBuilder();
					builder.append("网关数据出现异常\n未处理数为:").append(unHandleNumber);
					builder.append('\n').append("在线终端数:").append(onlineNumber);
					builder.append('\n').append("系统将在").append(delay).append("分钟后重启网关,请及时处理!");
					MailSenderRunner.start(builder.toString());
					GaterRestartRunner.start(lock);
					isStopping.set(true);
				}else{
					logger.info("检测网关数据结束");
					Thread.sleep(checkInterval * 60 * 1000);
				}
			} catch (Exception e) {
				logger.error("检测线程出现异常:",e);
				try{
					Thread.sleep(checkInterval * 60 * 1000);
				}catch(Exception ex){}
			}
		}
	}
}
