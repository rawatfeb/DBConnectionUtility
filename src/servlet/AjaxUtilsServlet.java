package servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import service.Services;
import utility.Utility;
import beans.RequestValueObject;
import context.DBConnectionUtilityContext;
import dao.DBConnectionHibernateDao;

/**
 * Servlet implementation class AjaxUtilsServlet
 */
public class AjaxUtilsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AjaxUtilsServlet() {
		super();
		//Load the DBConnectionUtilityContext class first as it is required to read configuration
		System.out.println(DBConnectionUtilityContext.ORACLE_DRIVER_CLASS);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		RequestValueObject requestValueObject = new RequestValueObject();
		requestValueObject.setCommand(request.getParameter("command"));

		//get_pool_or_domain_html for drop down html

		switch (requestValueObject.getCommand().toLowerCase()) {
		case "refresh":
			try {
				//dump everything as fresh from database
				response.setContentType("text/plain;charset=UTF-8");
				new Services().refreshLocalDB();
			} catch (Exception e) {
				response.getWriter().write(Utility.stack2string(e));
				e.printStackTrace();
				return;
			}
			response.getWriter().write("ok");
			break;

		case "getallresources":
			response.setContentType("text/plain;charset=UTF-8");
			response.getWriter().write(DBConnectionUtilityContext.getAllResourceCache(request.getParameter("q")));
			break;
		case "getallservices":
			response.setContentType("text/plain;charset=UTF-8");
			response.getWriter().write(DBConnectionUtilityContext.getAllServicesCache(request.getParameter("q")));
			break;
		case "getallservers":
			response.setContentType("text/plain;charset=UTF-8");
			response.getWriter().write(DBConnectionUtilityContext.getAllHostCache(request.getParameter("q")));
			break;
		default:
			response.setContentType("text/plain;charset=UTF-8");
			response.getWriter().write("Exception "+requestValueObject.getCommand().toLowerCase()+" Command not Found");
			break;
		}
		return ;
	}
}
