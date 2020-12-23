package com.cts.cj.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.Occupation;
import com.cts.cj.domain.Post;
import com.cts.cj.domain.PostUrl;
import com.cts.cj.domain.User;
import com.cts.cj.repository.Neo4jQueries;

@Repository
@Service
public class ConnectionServiceImpl implements ConnectionService{

	@Autowired
	private SessionFactory session;
	@Override
	public List<User> getConnections(String email) {
		List<User> connectionUser = new ArrayList<User>();
		
		Iterable<Map<String, Object>> userlist = session.openSession()
				.query(Neo4jQueries.STR_CONNECTIONS_CQL, Collections.singletonMap("emailId", email))
				.queryResults();
		userlist.forEach(r -> {
			User dbUser = (User) r.get("c");
			dbUser.setConnectionDate(String.valueOf(r.get("b.ConnectionDate")));
			List<Occupation> dbocc= new ArrayList<Occupation>();
			Occupation dbOccupation= (Occupation)r.get("e");
			dbocc.add(dbOccupation);
			
			
			
			if (connectionUser.size() == 0) {
				dbUser.setOccupation(dbocc);
				connectionUser.add(dbUser);
			} else {
				boolean dataMatched = false;
				for (int i = 0; i < connectionUser.size(); i++) {
					User p = connectionUser.get(i);
					if (p.getId().compareTo(dbUser.getId()) == 0) {
						List<Occupation> urllist = p.getOccupation();
						if (urllist != null) {
							urllist.add((Occupation) r.get("e"));
							p.setOccupation(urllist);
						}
						dataMatched = true;
					}
				}
				if (!dataMatched) {
					dbUser.setOccupation(dbocc);
					connectionUser.add(dbUser);
				}
			}
			
		});
		
		return connectionUser;
	}

	
}
