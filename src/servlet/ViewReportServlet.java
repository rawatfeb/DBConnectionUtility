package servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utility.Utility;
import dao.DBConnectionHibernateDao;

/**
 * Servlet implementation class ViewReportServlet
 */
public class ViewReportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger log = LogManager.getRootLogger();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ViewReportServlet() {
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
		String html = null;
		try {
			Map<String, Integer> bag = null;
			try {
				bag = Utility.readFromXmlFile();
			} catch (IOException e) {
				bag = new DBConnectionHibernateDao().prepareDailyReport();
			}
			html =Utility.generateHtmlResCon(bag);
		} catch (Exception e) {
			log.error(e.getMessage());
			request.setAttribute("exp", e.getMessage());
		}
		request.setAttribute("html", html);
		request.getRequestDispatcher("jsp/viewReport.jsp").forward(request, response);
	}
}
