package timer;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import context.DBConnectionUtilityContext;
import service.Services;

public class DBConnectionUtilityTimer extends TimerTask {

	Timer timer = new Timer();
	Logger log = LogManager.getRootLogger();

	public DBConnectionUtilityTimer() {

	}

	public void startRefreshTimer() {
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.set(Calendar.HOUR_OF_DAY,
				DBConnectionUtilityContext.getHourAtRefresh());
		tomorrow.set(Calendar.MINUTE, 0);
		tomorrow.set(Calendar.SECOND, 0);
		tomorrow.add(Calendar.DATE, 1);//setting tomorrow date  (+1)
		timer.schedule(this, tomorrow.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
	}

	@Override
	public void run() {
		log.debug("running timer task job .... " + new Date(System.currentTimeMillis()));
		try {
			new Services().refreshLocalDB();
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("not a successful refresh " + e.getMessage());
		}
	}
}
