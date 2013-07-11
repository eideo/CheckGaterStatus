package com.autotoll.gps.thread;

import org.apache.log4j.Logger;

import com.autotoll.gps.check.Config;

public class GaterRestartRunner implements Runnable{
	private static final Logger logger = Logger.getLogger(GaterRestartRunner.class);
	private Object lock = null;
	private GaterRestartRunner(Object lock){
		this.lock = lock;
	}
	public static void start(Object obj) {
		Thread t = new Thread(new GaterRestartRunner(obj));
		t.start();
	}
	@Override
	public void run() {
		int delay = Config.getInt("gater.restart.delay", 25);
		try {
			Thread.sleep(delay * 60 * 1000);
			 logger.info("开始关闭网关");
			 String[] stopShell = new String[]{"/bin/sh", "-c", Config.getProperty("gater.stop.path")};
			 Runtime.getRuntime().exec(stopShell);
			 logger.info("关闭网关结束");
			 logger.info("开始启动网关");
			 String[] startShell = new String[]{"/bin/sh", "-c", Config.getProperty("gater.start.path")};
			 Runtime.getRuntime().exec(startShell);
			 logger.info("启动网关结束");
			 
		} catch (Exception e) {
			logger.error("重启网关出错",e);
		}finally{
			synchronized (lock){
				 if(lock != null){
					 lock.notifyAll();
				 }
			 }
		}
	}

}