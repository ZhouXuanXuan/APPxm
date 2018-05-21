package cn.appsys.service.backend;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import cn.appsys.dao.backenduser.BackendUserMapper;
import cn.appsys.pojo.BackendUser;

@Service
public class BackendUserServiceImpl implements BackendUserService {
	@Resource
	private BackendUserMapper mapper;
	
	@Override
	public BackendUser login(String userCode, String userPassword) {
		// TODO Auto-generated method stub
		BackendUser user = null;
		try {
			user = mapper.getLoginUser(userCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//匹配密码
		if(null != user){
			if(!user.getUserPassword().equals(userPassword))
				user = null;
		}
		return user;
	}

}
