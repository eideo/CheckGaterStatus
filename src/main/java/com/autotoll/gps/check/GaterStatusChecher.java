package com.autotoll.gps.check;

import org.apache.log4j.Logger;

import com.autotoll.gps.thread.CheckerThread;
import com.autotoll.gps.thread.DaemonThread;

public class GaterStatusChecher {
	public static final Logger logger = Logger.getLogger(GaterStatusChecher.class);
	public static void main(String[] args) throws InterruptedException {
		CheckerThread.check();
		logger.info("成功启动检测服务");
		DaemonThread.start();
	}
	
}
