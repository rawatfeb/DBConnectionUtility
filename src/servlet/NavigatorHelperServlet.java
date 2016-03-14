package servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utility.Utility;
import beans.RequestValueObject;
import dao.DBConnectionHibernateDao;

/**
 * Servlet implementation class NavigatorHelperServlet
 */
public class NavigatorHelperServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NavigatorHelperServlet() {
		super();
		// TODO Auto-generated constructor stub
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
		switch (requestValueObject.getCommand().toLowerCase()) {
		case "getbyresource":
			response.setContentType("text/html;charset=UTF-8");
			Map<String, Integer> map = new DBConnectionHibernateDao().getByResource(request.getParameter("resource"));
			response.getWriter().write(Utility.generateHtmlTableServiceCon(map, request.getParameter("resource")));
			break;
		case "getbyserviceclass":
			response.setContentType("text/html;charset=UTF-8");
			Map<String, Integer> resMap = new DBConnectionHibernateDao().getByServiceClass(
					request.getParameter("serviceclass"), request.getParameter("resource"));
			response.getWriter().write(Utility.generateHtmlTableEngineCon(resMap));
			break;
		default:
			response.setContentType("text/plain;charset=UTF-8");
			response.getWriter().write(
					"Exception " + requestValueObject.getCommand().toLowerCase() + " Command not Found");
			break;
		}
		return;
	}

}
