package servlet;

import java.util.Properties;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import context.DBConnectionUtilityContext;



@RestController
@RequestMapping(value = "/config" )
public class ConfigController {
	org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getRootLogger();

	@RequestMapping(method = RequestMethod.GET)
	public Properties getConfiguarion() {
		log.debug("/config GET rest call has been called ");
		return DBConnectionUtilityContext.getConfigurableProperties();
	}

	@RequestMapping( method = RequestMethod.POST)
	public ResponseEntity<Properties> checkFile(@RequestBody Properties configuration) {
		log.debug("/config POST rest call has been called configuration="+configuration);
		configuration=DBConnectionUtilityContext.updateConfiguration(configuration);
		return new ResponseEntity<Properties>(configuration, HttpStatus.OK);
	}
}
