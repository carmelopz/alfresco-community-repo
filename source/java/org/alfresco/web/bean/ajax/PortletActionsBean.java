package org.alfresco.web.bean.ajax;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.web.app.servlet.ajax.InvokeCommand;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean backing the ajax requests from various Portlet webscripts.
 * 
 * @author Mike Hatfield
 */
public class PortletActionsBean implements Serializable
{
   private static final long serialVersionUID = -8230154592621310289L;
   
   private static Log logger = LogFactory.getLog(PortletActionsBean.class);
   
   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void deleteItem() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String strNodeRef = (String)requestMap.get("noderef");
      if (strNodeRef != null && strNodeRef.length() != 0)
      {
         try
         {
            Repository.getServiceRegistry(fc).getFileFolderService().delete(new NodeRef(strNodeRef));
            out.write("OK: " + strNodeRef);
         }
         catch (Throwable err)
         {
            out.write("ERROR: " + err.getMessage());
         }
      }
   }

   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void checkoutItem() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String strNodeRef = (String)requestMap.get("noderef");
      if (strNodeRef != null && strNodeRef.length() != 0)
      {
         try
         {
            Repository.getServiceRegistry(fc).getCheckOutCheckInService().checkout(new NodeRef(strNodeRef));
            out.write("OK: " + strNodeRef);
         }
         catch (Throwable err)
         {
            out.write("ERROR: " + err.getMessage());
         }
      }
   }

   @InvokeCommand.ResponseMimetype(value=MimetypeMap.MIMETYPE_HTML)
   public void checkinItem() throws Exception
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResponseWriter out = fc.getResponseWriter();
      
      Map<String, String> requestMap = fc.getExternalContext().getRequestParameterMap();
      String strNodeRef = (String)requestMap.get("noderef");
      if (strNodeRef != null && strNodeRef.length() != 0)
      {
         try
         {
            Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
            props.put(Version.PROP_DESCRIPTION, "");
            props.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
            Repository.getServiceRegistry(fc).getCheckOutCheckInService().checkin(new NodeRef(strNodeRef), props);
            out.write("OK: " + strNodeRef);
         }
         catch (Throwable err)
         {
            out.write("ERROR: " + err.getMessage());
         }
      }
   }
}