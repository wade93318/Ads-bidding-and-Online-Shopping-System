package rpc;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.AdItem;

/**
 * Servlet implementation class Ad
 */
@WebServlet("/Ad")
public class Ad extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Ad() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		DBConnection conn = DBConnectionFactory.getConnection();
		
		try {
			JSONObject ad = new JSONObject();
			JSONArray array = new JSONArray();
			
			AdItem adWithHighestRank = null;
			AdItem adWithSecondHighestRank = null;
			
			List<AdItem> items = conn.searchAdItems();
			
			if(items.size() < 2) {
				return;
			}
			
			AdItem ad0 = items.get(0);
			AdItem ad1 = items.get(1);
			
			float adRank0 = ad0.getAd_score() * ad0.getBid();
			float adRank1 = ad1.getAd_score() * ad1.getAd_id();
			
			if(adRank0 >= adRank1) {
				adWithHighestRank = ad0;
				adWithSecondHighestRank = ad1;
			}else {
				adWithHighestRank = ad1;
				adWithSecondHighestRank = ad0;
			}
			ad = adWithHighestRank.toJSONObject();
			
			for(int i = 2; i < items.size(); i++) {
				AdItem item = items.get(i);
				float adRankScore = item.getAd_score() * item.getBid();
				
				if(adRankScore > adWithHighestRank.getAd_score() * adWithHighestRank.getBid()) {
					adWithHighestRank = item;
					adWithSecondHighestRank = adWithHighestRank;
				}else if(adRankScore > adWithSecondHighestRank.getAd_score() * adWithSecondHighestRank.getBid()) {
					adWithSecondHighestRank = item;
				}	
			}
			double secondHighestAdRankScore = adWithSecondHighestRank.getAd_score() * adWithSecondHighestRank.getBid();
			double cost = secondHighestAdRankScore / adWithSecondHighestRank.getAd_score() + 0.01;
			System.out.println("cost is:" + cost);
			
			int advertiser_id = adWithHighestRank.getAdvertiser_id();
			double curBudget = conn.getBudget(advertiser_id);
			
			conn.updateBudget(advertiser_id, curBudget - cost);
			curBudget = conn.getBudget(advertiser_id);
			array.put(curBudget);
			
			RpcHelper.writeJsonArray(response, array);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			conn.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
		
		
	}

}
