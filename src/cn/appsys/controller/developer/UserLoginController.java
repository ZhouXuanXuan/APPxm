package cn.appsys.controller.developer;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;

import cn.appsys.pojo.AppCategory;
import cn.appsys.pojo.AppInfo;
import cn.appsys.pojo.AppVersion;
import cn.appsys.pojo.DataDictionary;
import cn.appsys.pojo.DevUser;
import cn.appsys.service.developer.AppCategoryService;
import cn.appsys.service.developer.AppInfoService;
import cn.appsys.service.developer.AppVersionService;
import cn.appsys.service.developer.DataDictionaryService;
import cn.appsys.service.developer.DevUserService;
import cn.appsys.tools.Constants;
import cn.appsys.tools.PageSupport;

@Controller
@RequestMapping("/dev")
public class UserLoginController {

	@Resource
	private DevUserService devUserService;
	@Resource
	private AppInfoService appInfoService;
	@Resource
	private AppCategoryService appCategoryService;
	@Resource
	private DataDictionaryService dataDictionaryService;
	@Resource
	private AppVersionService appVersionService;


	/**
	 * 前台登录
	 * @param devCode
	 * @param devPassword
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/flatform")
	public String dulogin(@RequestParam String devCode,@RequestParam String devPassword,HttpSession session ) throws Exception  {
		DevUser devUser = devUserService.login(devCode, devPassword);//调用方法根据返回值确定是否登录成功
		if(devUser.getDevPassword().equals(devPassword)) {//判断密码是否正确
			session.setAttribute(Constants.DEV_USER_SESSION, devUser);//吧用户存到回话里面去
			return "developer/main";

		}else {
			return "redirect:/403.jsp";
		}
	}
	
	/**
	 *开发者平台 入口
	 * @return
	 */
	@RequestMapping("/login")
	public String xianshi()  {
		return "devlogin";

	}
	/**
	 * 前台后台选项
	 * @return
	 */
	@RequestMapping("/login.do")
	public String  dev() {
		return "redirect:/index.jsp";
	}

	/**
	 * 用户注销
	 * @param request
	 * @return
	 */
	@RequestMapping("/logout")
	public String logout(HttpServletRequest request) {
		request.getSession().getAttribute(Constants.DEV_USER_SESSION); 
		return "devlogin";
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
		/*使用两个变量名来接受页面上的软件名称*/
		String typeCode = request.getParameter("typeCode");
		String querySoftwareName = request.getParameter("querySoftwareName");
		/*将获取到的所有APP信息和页面数量等数据传入会话*/
		List<AppInfo> appInfo=appInfoService.getAppInfoList(querySoftwareName, queryStatus, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId, currentPageNo, pageSize);
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
			totalCount = appInfoService.getAppInfoCount(querySoftwareName, queryStatus, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId);
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
			appInfo = appInfoService.getAppInfoList(querySoftwareName, queryStatus, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId, currentPageNo, pageSize);
			list2 = this.getDataDictionaryList("APP_STATUS");//
			list4 = this.getDataDictionaryList("APP_FLATFORM");//
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
		return "developer/appinfolist";
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

	/**
	 * 根据typeCode查询出相应的数据字典列表
	 * @param pid
	 * @return
	 */
	@RequestMapping(value="/datadictionarylist.json",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object getDataDicList (@RequestParam String tcode){
		return JSONArray.toJSONString(this.getDataDictionaryList(tcode)); 
	}

	/**
	 * 二级分类
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryCategoryLevel2",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object queryCategoryLevel2(HttpServletRequest request) throws Exception {
		Integer parentId=Integer.parseInt(request.getParameter("pid"));
		List<AppCategory> list=appCategoryService.getAppCategoryListByParentId(parentId);
		return JSON.toJSONString(list);
	}

	/**
	 * 三级分类
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryCategoryLevel3",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object queryCategoryLevel3(HttpServletRequest request) throws Exception {
		Integer parentId=Integer.parseInt(request.getParameter("pid"));
		List<AppCategory> list=appCategoryService.getAppCategoryListByParentId(parentId);
		return JSON.toJSONString(list);
	}

	/**
	 * 一级分类
	 * @param request
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryCategoryLevel1",method=RequestMethod.GET,produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object queryCategoryLevel1(HttpServletRequest request,@RequestParam(value="pid",required=false) Integer pid) throws Exception {
		List<AppCategory> list=appCategoryService.getAppCategoryListByParentId(pid);
		return JSON.toJSONString(list);
	}


	/**
	 * 删除操作
	 * @param id
	 * @return
	 */
	@RequestMapping("/delapp")
	@ResponseBody
	public String delapp(@RequestParam String id) {
		HashMap<String, String>result = new HashMap<String,String>();
		if(StringUtils.isNullOrEmpty(id)) {
			result.put("delResult", "notexist");
		}else {
			try {
				if(appInfoService.appsysdeleteAppById(Integer.parseInt(id))) {
					result.put("delResult", "true");
				}else {
					result.put("delResult", "false");
				}
			}catch(NumberFormatException e) {
				e.printStackTrace();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return JSONArray.toJSONString(result);
	}

	/*进入新增页面*/
	@RequestMapping("/appinfoadd")
	public String appinfoadd(@ModelAttribute("appInfo") AppInfo appInfo) {
		return "developer/appinfoadd";
	}

	/**
	 * 新增操作
	 * @param appInfo
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/appinfoaddsave")
	public String appinfoaddsave(AppInfo appInfo) throws Exception {
		boolean flag=	appInfoService.add(appInfo);//调用app新增方法
		if (flag) {
			return "redirect:/dev/list";//添加成功跳入app列表页面
		}
		return "developer/appversionadd";//添加失败跳入app新增页面继续新增
	}


	/**
	 * 增加appversion信息（跳转到新增app版本页面）
	 * @param appId
	 * @param fileUploadError
	 * @param appVersion
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/appversionadd")
	public String addVersion(@RequestParam(value="id")String appId, @RequestParam(value="error",required= false)String fileUploadError,AppVersion appVersion,Model model) {
		if(null !=fileUploadError && fileUploadError.equals("error1")) {//判断apk信息是否完整
			fileUploadError = Constants.FILEUPLOAD_ERROR_1;
		}else if(null !=fileUploadError && fileUploadError.equals("error2")) {//判断信息上传是否成功
			fileUploadError = Constants.FILEUPLOAD_ERROR_2;
		}else if(null !=fileUploadError && fileUploadError.equals("error3")) {//判断信息上传文件的大小
			fileUploadError = Constants.FILEUPLOAD_ERROR_3;
		}
		appVersion.setAppId(Integer.parseInt(appId));
		List<AppVersion> appVersionList = null;
		try {
			appVersionList = appVersionService.getAppVersionList(Integer.parseInt(appId));
			appVersion.setAppName((appInfoService.getAppInfo(Integer.parseInt(appId),null)).getSoftwareName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("appVersionList",appVersionList);
		model.addAttribute(appVersion);
		model.addAttribute("fileUploadError",fileUploadError);
		//跳转到新增app版本页面
		return "developer/appversionadd";
	}




	/**
	 * 保存新增appversion数据（子表）-上传该版本的apk包
	 * @param appVersion
	 * @param session
	 * @param request
	 * @param attach
	 * @return
	 */
	@RequestMapping(value="/addversionsavess")
	public String addVersionSave(AppVersion appVersion,HttpSession session,HttpServletRequest request,@RequestParam(value="a_downloadLink",required= false) MultipartFile attach ) {
		String downloadLink = null;
		String apkLocPath = null;
		String apkFileName = null;
		if(!attach.isEmpty()) {
			String path = request.getSession().getServletContext().getRealPath("statics"+File.separator+"uploadfiles");
			String oldFileName = attach.getOriginalFilename();
			String prefix = FilenameUtils.getExtension(oldFileName);
			if(prefix.equalsIgnoreCase("apk")) {
				String apkName = null;
				try {
					apkName = appInfoService.getAppInfo(appVersion.getAppId(),null).getAPKName();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(apkName == null || "".equals(apkName)) {
					return "redirect:/appversionadd?id="+appVersion.getAppId()+"&error=error1";
				}
				apkFileName = apkName+"-"+appVersion.getVersionNo()+".apk";
				File targetFile = new File(path,apkFileName);
				if(!targetFile.exists()) {
					targetFile.mkdirs();
				}
				try {
					attach.transferTo(targetFile);
				}catch(Exception e) {
					e.printStackTrace();
					return "redirect:/appversionadd?id="+appVersion.getAppId()+"&error=error2";
				}
				downloadLink = request.getContextPath()+"/statics/uploadfiles/"+apkFileName;
				apkLocPath = path+File.separator+apkFileName;
			}else {
				return "redirect:/dev/appversionadd?id="+appVersion.getAppId()+"&error=error3";
			}
		}
		appVersion.setCreatedBy(((DevUser)session.getAttribute(Constants.DEV_USER_SESSION)).getId());
		appVersion.setCreationDate(new Date());
		appVersion.setDownloadLink(downloadLink);
		appVersion.setApkLocPath(apkLocPath);
		appVersion.setApkFileName(apkFileName);
		try {
			if(appVersionService.appsysadd(appVersion)) {
				return "redirect:/list";
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "redirect:/appversionadd?id="+appVersion.getAppId();
	}

	@RequestMapping("/{appid}/sale")
	@ResponseBody
	public Object sale(@PathVariable String appid,HttpSession session){
		HashMap <String,Object> resultMap = new HashMap<String,Object>();
		Integer appIdInteger = 0;
		try {
			appIdInteger = Integer.parseInt(appid);
		}catch(Exception e) {
			appIdInteger = 0;
		}
		resultMap.put("errorCode", "0");
		resultMap.put("appId",appid);
		if(appIdInteger>0) {
			try {
				DevUser devUser = (DevUser)session.getAttribute(Constants.DEV_USER_SESSION);
				AppInfo appInfo = new AppInfo();
				appInfo.setId(appIdInteger);
				appInfo.setModifyBy(devUser.getId());
				if(appInfoService.appsysUpdateSaleStatusByAppId(appInfo)) {
					resultMap.put("resultMsg","success");
				}else {
					resultMap.put("resultMsg", "success");
				}
			}catch(Exception e) {
				resultMap.put("errorCode", "exception000001");
			}
		}else {
			resultMap.put("errorCode", "param000001");
		}
		return resultMap;
	}

	//判断APKName是否唯一
	@RequestMapping("/apkexist")
	@ResponseBody
	public Object apkexist(@RequestParam String APKName){
		HashMap<String,String> resultMap = new HashMap<String,String>();
		if(StringUtils.isNullOrEmpty(APKName)) {
			resultMap.put("APKName", "empty");
		}else {
			AppInfo appInfo = null;
			try {
				appInfo = appInfoService.getAppInfo(null, APKName);
			}catch(Exception e) {
				e.printStackTrace();
			}
			if(null !=appInfo) {
				resultMap.put("APKName","exist");
			}else {
				resultMap.put("APKName", "noexist");
			}
		}
		return JSONArray.toJSONString(resultMap);
	}

	/**
	 * 查看app信息，包括app基本信息和版本信息列表（跳转到查看页面）
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/appview/{id}")
	public String view(@PathVariable String id,Model model) {
		AppInfo appInfo = null;
		List<AppVersion> appVersionList = null;
		try {
			appInfo = appInfoService.getAppInfo(Integer.parseInt(id), null);
			appVersionList = appVersionService.getAppVersionList(Integer.parseInt(id));
		}catch(Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("appVersionList",appVersionList);
		model.addAttribute(appInfo);
		return "developer/appinfoview";
	}


	/**
	 * 修改appInfo信息（跳转到修改appInfo页面）
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/appinfomodify")
	public String appinfomodify(HttpServletRequest request) throws Exception {
		Integer id=Integer.parseInt(request.getParameter("id"));
		AppInfo appInfo=appInfoService.getAppInfo(id, null);
		request.getSession().setAttribute("appInfo",appInfo);
		return "developer/appinfomodify";
	}
	/**
	 * 保存修改后的appInfo
	 * @param appInfo
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/appinfomodifysave")
	public String modifySave(AppInfo appInfo) throws Exception {
		boolean flag=appInfoService.modify(appInfo);
		if(flag) {
			return "redirect:/dev/list";
		}else {
			return "redirect:/dev/appinfomodifysave";
		}
	}
	
	
	
	

	/**
	 * 修改最新的appVersion信息（跳转到修改appVersion页面）
	 * @param versionId
	 * @param appId
	 * @param fileUploadError
	 * @param model
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/appversionmodifyt")
	public String modifyAppVersion(@RequestParam int vid,@RequestParam int aid,Model model) throws Exception {
		AppVersion appVersion = appVersionService.getAppVersionById(vid);
		List<AppVersion> appVersionList = appVersionService.getAppVersionList(aid);

		model.addAttribute(appVersion);
		model.addAttribute(appVersionList);
		return "developer/appversionmodify";
	}

	/**
	 * 保存修改后的aooversion
	 * @param appVersion
	 * @param session
	 * @param request
	 * @param attach
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/appversionmodifysave")
	public String modifyAppversionSave(AppVersion appVersion) throws Exception {
		boolean flag=	appVersionService.modify(appVersion);
		if (flag) {
			return "redirect:/dev/list";
		}
		return "redirect:/dev/appversionmodifysave";
	}

	/**
	 * 修改操作时，删除文件（logo图片/apk文件），并更新数据库（app_info/app_version）
	 * @param flag
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delfile")
	@ResponseBody
	public Object delFile(@RequestParam(value="flag",required=false) String flag,@RequestParam(value="id",required=false) String id){
		HashMap<String, String> resultMap = new HashMap<String, String>();
		String fileLocPath = null;
		if(flag == null || flag.equals("") ||
				id == null || id.equals("")){
			resultMap.put("result", "failed");
		}else if(flag.equals("logo")){
			try {
				fileLocPath = (appInfoService.getAppInfo(Integer.parseInt(id), null)).getLogoLocPath();
				File file = new File(fileLocPath);
				if(file.exists())
					if(file.delete()){
						if(appInfoService.deleteAppLogo(Integer.parseInt(id))){
							resultMap.put("result", "success");
						}
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(flag.equals("apk")){
			try {
				fileLocPath = (appVersionService.getAppVersionById(Integer.parseInt(id))).getApkLocPath();
				File file = new File(fileLocPath);
				if(file.exists())
					if(file.delete()){
						if(appVersionService.deleteApkFile(Integer.parseInt(id))){
							resultMap.put("result", "success");
						}
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return JSONArray.toJSONString(resultMap);
	}

	@RequestMapping(value="/delapp.json")
	@ResponseBody
	public Object delApp(@RequestParam String id){
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(StringUtils.isNullOrEmpty(id)){
			resultMap.put("delResult", "notexist");
		}else{
			try {
				if(appInfoService.appsysdeleteAppById(Integer.parseInt(id)))
					resultMap.put("resultMsg", "success");
				else
					resultMap.put("resultMsg", "saleSwitch");
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return JSONArray.toJSONString(resultMap);
	}
}