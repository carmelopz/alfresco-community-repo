package org.alfresco.repo.avm.wf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

public class AVMSubmitPackageHandler extends JBPMSpringActionHandler implements
        Serializable 
{
    private static final long serialVersionUID = 4113360751217684995L;

    /**
     * The AVMService instance.
     */
    private AVMService fAVMService;
    
    /**
     * The AVMSyncService instance.
     */
    private AVMSyncService fAVMSyncService;

    /**
     * The NodeService reference.
     */
    private NodeService fNodeService;
    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        fAVMService = (AVMService)factory.getBean("AVMService");
        fAVMSyncService = (AVMSyncService)factory.getBean("AVMSyncService");
        fNodeService = (NodeService)factory.getBean("NodeService");
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("package")).getNodeRef();
        List<ChildAssociationRef> children = fNodeService.getChildAssocs(pkg);
        List<AVMDifference> diffs = new ArrayList<AVMDifference>();
        Map<String, String> storesHit = new HashMap<String, String>();
        for (ChildAssociationRef child : children)
        {
            NodeRef childRef = child.getChildRef();
            if (!fNodeService.hasAspect(childRef, ContentModel.ASPECT_REFERENCES_NODE))
            {
                throw new AVMSyncException("Package node does not have cm:referencesnode.");
            }
            NodeRef toSubmit = (NodeRef)fNodeService.getProperty(childRef, ContentModel.PROP_NODE_REF);
            Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(toSubmit);
            String avmPath = versionPath.getSecond();
            String [] storePath = avmPath.split(":");
            String websiteName = fAVMService.getStoreProperty(storePath[0], 
                                                              QName.createQName(null, ".website.name")).
                                                              getStringValue();
            String stagingName = websiteName + "-staging";
            AVMDifference diff = 
                new AVMDifference(-1, avmPath,
                                  -1, stagingName + ":" + storePath[1],
                                  AVMDifference.NEWER);
            diffs.add(diff);
            storesHit.put(storePath[0], stagingName);
        }
        fAVMSyncService.update(diffs, true, true, false, false);
        for (Map.Entry<String, String> entry : storesHit.entrySet())
        {
            fAVMSyncService.flatten(entry.getKey() + ":/appBase", 
                                    entry.getValue() + ":/appBase");
        }
    }
}
