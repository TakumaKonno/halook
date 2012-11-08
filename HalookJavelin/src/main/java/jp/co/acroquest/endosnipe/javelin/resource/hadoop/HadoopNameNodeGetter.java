/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Acroquest Technology Co.,Ltd.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package jp.co.acroquest.endosnipe.javelin.resource.hadoop;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import jp.co.acroquest.endosnipe.common.config.JavelinConfig;
import jp.co.acroquest.endosnipe.common.entity.DisplayType;
import jp.co.acroquest.endosnipe.common.entity.ItemType;
import jp.co.acroquest.endosnipe.common.entity.ResourceItem;
import jp.co.acroquest.endosnipe.common.logger.SystemLogger;
import jp.co.acroquest.endosnipe.javelin.converter.hadoop.HadoopMeasurementInfo;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

/**
 * Hadoop NameNode �̏����擾����Getter�B
 * 
 * @author Ochiai
 */
public class HadoopNameNodeGetter extends HadoopGetter
{
    /** MBeanServer */
    private static MBeanServer  server__                =
                                                          ManagementFactory.getPlatformMBeanServer();

    /** NameNode �� JMX �� ObjectName ��\�������� */
    private static final String OBJECT_NAME_NAMENODE    = "hadoop:service=NameNode,name=NameNodeInfo";

    /** HDFS�S�̂̋󂫗e�ʂ�Attribute */
    private static final String ATTRIBUTE_DFS_REMAINING = "Free";

    /** HDFS�S�̂̎g�p�ʂ�Attribute */
    private static final String ATTRIBUTE_DFS_USED      = "Used";

    /** LiveNode��Attribute */
    private static final String ATTRIBUTE_LIVE_NODES    = "LiveNodes";

    /** dfsused �� suffix */
    private static final String SUFFIX_DFSUSED          = "/dfsused";

    /** dfsremaining �� suffix */
    private static final String SUFFIX_DFSREMAINING     = "/dfsremaining";
    
    /** ���̑��m�[�h��� �� suffix */
    private static final String SUFFIX_NODEINFO     = "/nodeinfo";

    /** hdfs �� prefix */
    private static final String PREFIX_HDFS             = "/hdfs";

    /** hdfs �S�̂�\�������� */
    private static final String HDFS_ALL                = "/--all--";

    /** JMX�̌��ʂ́AusedSpace��\��key */
    private static final String KEY_USED_SPACE          = "usedSpace";

    /** Javelin�ݒ� */
    private JavelinConfig config_              = new JavelinConfig();
    
    /**
     * JMX�̌v���l���擾���邽�߂̃N���X�����������܂��B
     *
     */
    public HadoopNameNodeGetter()
    {
    }

    /**
     * JMX�v���l���擾���܂��B<br />
     *
     * @return JMX�v���l�̃��X�g
     */
    public List<ResourceItem> getValues()
    {
        SystemLogger.getInstance().info("HadoopNameNodeGetter.getValues() : start");
        
        List<ResourceItem> returnList = new ArrayList<ResourceItem>();

        if (config_.isCollectHadoopAgentResources() == false)
        {
            return returnList;
        }
        
        // NameNode �̃f�[�^�擾�Ώ�ObjectName
        ObjectName nameNodeInfoObjectName = null;
        try
        {
            nameNodeInfoObjectName = new ObjectName(OBJECT_NAME_NAMENODE);
        }
        catch (MalformedObjectNameException ex)
        {
            SystemLogger.getInstance().warn(ex);
            return returnList;
        }

        // HDFS�S�̂̋󂫗e�ʁA�g�p�ʂ��擾����
        Number dfsRemaining = getJMXValueLong(nameNodeInfoObjectName, ATTRIBUTE_DFS_REMAINING);
        if (dfsRemaining != null)
        {
            ResourceItem dfsRemainingItem =
                                            createResourceItem(nameNodeInfoObjectName, PREFIX_HDFS
                                                                       + HDFS_ALL
                                                                       + SUFFIX_DFSREMAINING,
                                                               dfsRemaining,
                                                               ItemType.ITEMTYPE_LONG);
            returnList.add(dfsRemainingItem);

        }

        Number dfsUsed = getJMXValueLong(nameNodeInfoObjectName, ATTRIBUTE_DFS_USED);
        if (dfsUsed != null)
        {
            ResourceItem dfsUsedItem =
                                       createResourceItem(nameNodeInfoObjectName, PREFIX_HDFS
                                                                  + HDFS_ALL + SUFFIX_DFSUSED,
                                                          dfsUsed,
                                                          ItemType.ITEMTYPE_LONG);
            returnList.add(dfsUsedItem);
        }

        try
        {
            // JMX�ŊeDataNode�̎g�p�ʂ��܂�JSON�`���̕�������擾����B
            // �擾����f�[�^�͈ȉ��̌`�������Ă���
            // {
            //    "hostname1":{"usedSpace":3151212544,"lastContact":1},
            //    "hostname2":{"usedSpace":3151212544,"lastContact":1}
            // }
            String liveNodesJson =
                                   server__.getAttribute(nameNodeInfoObjectName,
                                                         ATTRIBUTE_LIVE_NODES).toString();

            Map<String, Map<String, Number>> liveNodes = JSON.decode(liveNodesJson);
            
            List<String> inputNames = new ArrayList<String>();
            inputNames.addAll(liveNodes.keySet());
            List<String> resolvedNames = HadoopMeasurementInfo.getInstance().resolve(inputNames);
            for (int index = 0; index < inputNames.size(); index++)
            {
                String serverName = inputNames.get(index);
                String rackName = resolvedNames.get(index);
                StringBuilder builder = new StringBuilder();
                builder.append(PREFIX_HDFS);
                builder.append("/");
                builder.append(serverName);
                builder.append(SUFFIX_NODEINFO);

                String nodeInfoItemName = builder.toString();
                String nodeInfoValue = "{\"rack-name\":\"" + rackName + "\"}";
                ResourceItem datanodeDfsUsedItem =
                                                   createResourceItem(nameNodeInfoObjectName,
                                                                      nodeInfoItemName,
                                                                      nodeInfoValue,
                                                                      ItemType.ITEMTYPE_LONG);
                returnList.add(datanodeDfsUsedItem);

            }

            // TODO JMX�Ŏ��Ȃ����߁A�S�̂̋󂫗e�ʂ��m�[�h���Ŋ��������̂��b��I�ɊeDataNode�̋󂫗e�ʂƂ���
            long datanodeDfsTotal = 0;
            int mapLength = liveNodes.size();
            if (mapLength > 0 && dfsRemaining != null && dfsUsed != null)
            {
                long longDFSRemaining = dfsRemaining.longValue();
                long longDFSUsed = dfsUsed.longValue();
                datanodeDfsTotal = (longDFSRemaining + longDFSUsed) / mapLength;
            }

            for (Entry<String, Map<String, Number>> liveNode : liveNodes.entrySet())
            {
                String hostname = liveNode.getKey();
                long datanodeDfsUsed = liveNode.getValue().get(KEY_USED_SPACE).longValue();

                // ItemName �ƂȂ镶����
                StringBuilder builder = new StringBuilder();
                builder.append(PREFIX_HDFS);
                builder.append("/");
                builder.append(hostname);
                String dfsRemainingItemName = builder.toString() + SUFFIX_DFSREMAINING;
                String dfsUsedItemName = builder.toString() + SUFFIX_DFSUSED;

                ResourceItem datanodeDfsRemainingItem =
                                                        createResourceItem(nameNodeInfoObjectName,
                                                                           dfsRemainingItemName,
                                                                           (datanodeDfsTotal - datanodeDfsUsed),
                                                                           ItemType.ITEMTYPE_LONG);

                ResourceItem datanodeDfsUsedItem =
                                                   createResourceItem(nameNodeInfoObjectName,
                                                                      dfsUsedItemName,
                                                                      datanodeDfsUsed,
                                                                      ItemType.ITEMTYPE_LONG);
                returnList.add(datanodeDfsRemainingItem);
                returnList.add(datanodeDfsUsedItem);
            }
        }
        catch (AttributeNotFoundException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }
        catch (InstanceNotFoundException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }
        catch (MBeanException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }
        catch (ReflectionException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }
        catch (JSONException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }

        return returnList;
    }

    /**
     * JMX��long�l���擾����
     * 
     * @param objectName �擾����ΏۂƂȂ�I�u�W�F�N�g��
     * @param attribute �擾����ΏۂƂȂ鑮����
     * @return JMX�̒l���擾�������ʁilong�l�j
     */
    private Number getJMXValueLong(ObjectName objectName, String attribute)
    {
        Number valueObj = null;

        try
        {
            // JMX�̌v���l���擾����
            Object value = server__.getAttribute(objectName, attribute);

            if (value instanceof Number)
            {
                valueObj = (Number) value;
            }
            else
            {
                SystemLogger.getInstance().warn("Type error. objectName=" + objectName
                                                         + ",attribute=" + attribute + ",value="
                                                         + value);
            }
        }
        catch (AttributeNotFoundException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }
        catch (InstanceNotFoundException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }
        catch (MBeanException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }
        catch (ReflectionException ex)
        {
            SystemLogger.getInstance().warn(ex);
        }

        return valueObj;
    }

    /**
     * ResourceItem���쐬����
     * 
     * @param objectName �擾����ΏۂƂȂ�I�u�W�F�N�g��
     * @param name Item Name
     * @return JMX�̒l���擾�������ʂ� ResourceItem �̌`���ŕԂ�
     */
    private ResourceItem createResourceItem(ObjectName objectName, String name, Number value,
            ItemType itemType)
    {
        ResourceItem retValue = new ResourceItem();

        retValue.setValue(String.valueOf(value));
        retValue.setItemType(itemType);
        retValue.setObjectName(objectName.toString());
        retValue.setName(name);
        retValue.setObjectDisplayNeme(objectName.toString());
        retValue.setDisplayName(name);
        retValue.setDisplayType(DisplayType.DISPLAYTYPE_NORMAL);

        return retValue;
    }

    /**
     * ResourceItem���쐬����
     * 
     * @param objectName �擾����ΏۂƂȂ�I�u�W�F�N�g��
     * @param name Item Name
     * @return JMX�̒l���擾�������ʂ� ResourceItem �̌`���ŕԂ�
     */
    private ResourceItem createResourceItem(ObjectName objectName, String name, String value,
            ItemType itemType)
    {
        ResourceItem retValue = new ResourceItem();

        retValue.setValue(String.valueOf(value));
        retValue.setItemType(itemType);
        retValue.setObjectName(objectName.toString());
        retValue.setName(name);
        retValue.setObjectDisplayNeme(objectName.toString());
        retValue.setDisplayName(name);
        retValue.setDisplayType(DisplayType.DISPLAYTYPE_NORMAL);

        return retValue;
    }
    
    /**
     * {@inheritDoc}
     */
    public ItemType getItemType()
    {
        //return ItemType.ITEMTYPE_LONG;
        return ItemType.ITEMTYPE_STRING;
    }

}
