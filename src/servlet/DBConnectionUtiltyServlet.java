package servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import service.Services;
import timer.DBConnectionUtilityTimer;
import beans.RequestValueObject;
import entity.JvmInfo;

/**
 * Servlet implementation class DBConnectionUtiltyServlet
 */
public class DBConnectionUtiltyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger log = LogManager.getRootLogger();
	DBConnectionUtilityTimer dbConnectionUtilitytimer = new DBConnectionUtilityTimer();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DBConnectionUtiltyServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			dbConnectionUtilitytimer.startRefreshTimer();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		dbConnectionUtilitytimer.cancel();
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
		requestValueObject.setResource(request.getParameter("resource"));
		requestValueObject.setServiceHint(request.getParameter("serviceHint"));
		requestValueObject.setHosts(request.getParameter("hosts"));
		requestValueObject.setMinCon(request.getParameter("minCon"));

		System.out.println("Request made to tool requestValueObject=" + requestValueObject);

		log.trace("Request made to tool requestValueObject= " + requestValueObject);
		try {
			request.setAttribute("resp", getJvmInfoList(requestValueObject));
		} catch (Exception e) {
			request.setAttribute("exp", e.getMessage());
			e.printStackTrace();
		}
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}

	public List<JvmInfo> getJvmInfoList(RequestValueObject requestValueObject) throws Exception {
		return new Services().getJvmInfosFromLocal(requestValueObject);
	}
}
