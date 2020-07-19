package com.mkflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.DefaultBean;

import javax.enterprise.context.Dependent;
import javax.ws.rs.Produces;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 16/07/2020 19:26
 **/
@Dependent
public class BeanRegister {
	@Produces
	@DefaultBean
	public ObjectMapper configuration() {
		return new ObjectMapper();
	}

}
