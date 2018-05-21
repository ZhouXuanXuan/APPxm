package cn.appsys.controller.backend;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import cn.appsys.pojo.AppCategory;
import cn.appsys.pojo.AppInfo;
import cn.appsys.pojo.AppVersion;
import cn.appsys.pojo.BackendUser;
import cn.appsys.pojo.DataDictionary;
import cn.appsys.service.backend.AppService;
import cn.appsys.service.backend.BackendUserService;
import cn.appsys.service.developer.AppCategoryService;
import cn.appsys.service.developer.AppVersionService;
import cn.appsys.service.developer.DataDictionaryService;
import cn.appsys.tools.Constants;
import cn.appsys.tools.PageSupport;

@Controller
@RequestMapping("/manager")
public class AppLoginController {

	@Resource
	private BackendUserService backendUserService;
	@Resource
	private AppService appService;
	@Resource
	private AppCategoryService appCategoryService;
	@Resource
	private DataDictionaryService dataDictionaryService;
	@Resource
	private AppVersionService appVersionService;
	
	/**
	 * 显示选择前台后台页面
	 * @return
	 */
	@RequestMapping("/login")
	public String login() {
		return "backendlogin";
	}
	
	/**
	 * 后台登录
	 * @param userCode
	 * @param userPassword
	 * @param request
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/dologin")
	public String  doLogin(@RequestParam String userCode,@RequestParam String userPassword,HttpServletRequest request,HttpSession session) throws Exception {
		BackendUser backendUser = backendUserService.login(userCode, userPassword);
		if(backendUser.getUserPassword().equals(userPassword)) { //判断密码是否正确
			session.setAttribute(Constants.USER_SESSION, backendUser);
				return "backend/main";
		}else {
			return "redirect:/403.jsp";
		}
	}
	/**
	 * 后台注销
	 * @param session
	 * @return
	 */
	@RequestMapping(value="/backend/main")
	public String main(HttpSession session){
		if(session.getAttribute(Constants.USER_SESSION) == null){
			return "redirect:/manager/login";
		}
		return "backend/main";
	}
	
	@RequestMapping("/logout")
	public String logout(HttpServletRequest request) {
		request.getSession().getAttribute(Constants.DEV_USER_SESSION);//清除session
		return "backendlogin";
	}
	
	
	/**
	 * 显示全部 和 一级菜单二级菜单三级菜单的查询 显示APP列表
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/list")
	public String appinfolist(HttpServletRequest request,Model model) throws Exception {
		/*逐个判断是否为空*/
		Integer queryStatus = null;
		if(queryStatus != null && !queryStatus.equals("")){
			queryStatus = Integer.parseInt(request.getParameter("queryStatus"));
		}
		Integer queryCategoryLevel1 = null;
		if(queryCategoryLevel1 != null && !queryCategoryLevel1.equals("")){
			queryCategoryLevel1 = Integer.parseInt(request.getParameter("queryCategoryLevel1"));
		}
		Integer queryCategoryLevel2 = null;
		if(queryCategoryLevel2 != null && !queryCategoryLevel2.equals("")){
			queryCategoryLevel2 = Integer.parseInt(request.getParameter("queryCategoryLevel2"));
		}
		Integer queryCategoryLevel3 = null;
		if(queryCategoryLevel3 != null && !queryCategoryLevel3.equals("")){
			queryCategoryLevel3 = Integer.parseInt(request.getParameter("queryCategoryLevel3"));
		}
		Integer queryFlatformId = null;
		if(queryFlatformId != null && !queryFlatformId.equals("")){
			queryFlatformId = Integer.parseInt(request.getParameter("queryFlatformId"));
		}
		Integer devId = null;
		if(devId != null && !devId.equals("")){
			devId = Integer.parseInt(request.getParameter("devId"));
		}
		Integer pageSize = 5;
		String djf = request.getParameter("pageSize");
		if(djf != null && !djf.equals("")){
			pageSize = Integer.parseInt(djf);
		}
		Integer currentPageNo = 1;
		String sdjf = request.getParameter("pageIndex");
		if(sdjf != null && !sdjf.equals("")){
			currentPageNo = Integer.parseInt(sdjf);
		}
		Integer appId = null;
		String sedfb = request.getParameter("appId");
		if(sedfb != null && !sedfb.equals("")){
			appId = Integer.parseInt(sedfb);
		}
		String typeCode = request.getParameter("typeCode");
		String querySoftwareName = request.getParameter("querySoftwareName");
		/*将获取到的所有APP信息和页面数量等数据传入会话*/
		List<AppInfo> appInfo=appService.getAppInfoList(querySoftwareName, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, currentPageNo, pageSize);
		request.getSession().setAttribute("appInfoList", appInfo);
		List<AppCategory> list = appCategoryService.getAppCategoryListByParentId(null);
		List<DataDictionary> list2 = dataDictionaryService.getDataDictionaryList("APP_STATUS");
		request.setAttribute("statusList", list2);
		List<DataDictionary> list3 = dataDictionaryService.getDataDictionaryList("APP_FLATFORM");
		request.setAttribute("dataDictionary", list3);
		List<DataDictionary> list4 = dataDictionaryService.getDataDictionaryList("APP_FLATFORM");
		request.setAttribute("flatFormList", list4);
		request.setAttribute("categoryLevel1List", list);

		//总数量（表）
		int totalCount = 0;
		try {
			totalCount = appService.getAppInfoCount(querySoftwareName, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId);
		}catch(Exception e) {
			e.printStackTrace();
		}

		//总页数
		PageSupport pages = new PageSupport();
		pages.setCurrentPageNo(currentPageNo);
		pages.setPageSize(pageSize);
		pages.setTotalCount(totalCount);
		int totalPageCount = pages.getTotalPageCount();
		//控制首页和尾页
		if(currentPageNo < 1) {
			currentPageNo = 1;
		}else if(currentPageNo>totalPageCount){
			currentPageNo = totalPageCount;
		}
		try {
			appInfo = appService.getAppInfoList(querySoftwareName, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, currentPageNo, pageSize);
			list2 = this.getDataDictionaryList("APP_STATUS");
			list4 = this.getDataDictionaryList("APP_FLATFORM");
			list = appCategoryService.getAppCategoryListByParentId(null);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		model.addAttribute("appInfoList",appInfo);
		model.addAttribute("statusList",list2);
		model.addAttribute("flatFormList",list4);
		model.addAttribute("categoryLevel1List",list);
		model.addAttribute("pages", pages);
		model.addAttribute("queryStatus", queryStatus);
		model.addAttribute("querySoftwareName", querySoftwareName);
		model.addAttribute("queryCategoryLevel1", queryCategoryLevel1);
		model.addAttribute("queryCategoryLevel2", queryCategoryLevel2);
		model.addAttribute("queryCategoryLevel3", queryCategoryLevel3);
		model.addAttribute("queryFlatformId",queryFlatformId);
		
		//跳转到APP列表页面
		return "backend/applist";
	}
	/**
	 * 获取APP二级分类的方法
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryCategoryLevel2",produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object queryCategoryLevel2(HttpServletRequest request) throws Exception {
		Integer parentId=Integer.parseInt(request.getParameter("pid"));
		List<AppCategory> list=appCategoryService.getAppCategoryListByParentId(parentId);
		return JSON.toJSONString(list);
	}
	/**
	 * 获取APP三级分类的方法
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryCategoryLevel3",produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object queryCategoryLevel3(HttpServletRequest request) throws Exception {
		Integer parentId=Integer.parseInt(request.getParameter("pid"));
		List<AppCategory> list=appCategoryService.getAppCategoryListByParentId(parentId);
		return JSON.toJSONString(list);
	}
	/**
	 * 获取APP一级分类的方法
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryCategoryLevel1",produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object queryCategoryLevel1(HttpServletRequest request,@RequestParam(value="pid",required=false) Integer pid) throws Exception {

		List<AppCategory> list=appCategoryService.getAppCategoryListByParentId(pid);
		return JSON.toJSONString(list);
	}
	
	public List<DataDictionary> getDataDictionaryList(String typeCode){
		List<DataDictionary> dataDictionaryList = null;
		try {
			dataDictionaryList = dataDictionaryService.getDataDictionaryList(typeCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataDictionaryList;
	}
	
	public List<AppCategory> getCategoryList (String pid){
		List<AppCategory> categoryLevelList = null;
		try {
			categoryLevelList = appCategoryService.getAppCategoryListByParentId(pid==null?null:Integer.parseInt(pid));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return categoryLevelList;
	}
	
	/**
	 * 跳转到APP信息审核页面
	 * @param appId
	 * @param versionId
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/check")
	public String check(@RequestParam(value="aid",required=false) String appId,@RequestParam(value="vid",required=false) String versionId,Model model) {
		AppInfo appInfo = null;
		AppVersion appVersion = null;
		try {
			appInfo = appService.getAppInfo(Integer.parseInt(appId));
			appVersion = appVersionService.getAppVersionById(Integer.parseInt(versionId));
		}catch(Exception e) {
			e.printStackTrace();
		}
		model.addAttribute(appVersion);
		model.addAttribute(appInfo);
		return "backend/appcheck";
	}
	@RequestMapping("/checksave")
	public String checkSave(AppInfo appInfo) {
		try {
			if(appService.updateSatus(appInfo.getStatus(), appInfo.getId())) {
				return "redirect:/manager/list";
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "backend/appcheck";
	}
}
