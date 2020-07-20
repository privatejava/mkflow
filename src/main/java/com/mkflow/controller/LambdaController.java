package com.mkflow.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mkflow.service.HookHandlerService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Narayan <me@ngopal.com.np> - ngm
 * Created 20/07/2020 17:14
 **/
//@Named("jobHandler")
public class LambdaController { /*implements RequestHandler<Map, Map> {*/

//	private static Logger log = LoggerFactory.getLogger(LambdaController.class);
//
//	@Inject
//	HookHandlerService service;
//
//	@Override
//	public Map handleRequest(Map json, Context context) {
//		try {
//			return service.process(json);
//		} catch (IOException  | GitAPIException e) {
//			e.printStackTrace();
//			return new HashMap();
//		}
//	}
}
