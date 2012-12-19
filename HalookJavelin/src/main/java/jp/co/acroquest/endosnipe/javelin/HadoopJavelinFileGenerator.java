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
package jp.co.acroquest.endosnipe.javelin;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import jp.co.acroquest.endosnipe.common.config.JavelinConfig;
import jp.co.acroquest.endosnipe.common.logger.SystemLogger;
import jp.co.acroquest.endosnipe.javelin.converter.hadoop.HadoopCallTree;
import jp.co.acroquest.endosnipe.javelin.event.CommonEvent;
import jp.co.acroquest.endosnipe.javelin.log.JavelinFileGenerator;
import jp.co.acroquest.endosnipe.javelin.log.JavelinLogCallback;
import jp.co.acroquest.endosnipe.javelin.log.JavelinLogMaker;
import jp.co.acroquest.endosnipe.javelin.util.ThreadUtil;

/**
 * Javelin�̃��O�t�@�C�������Ǘ�����
 *
 * @author fujii
 *
 */
public class HadoopJavelinFileGenerator extends JavelinFileGenerator
{

    /**
     * �R���X�g���N�^�B
     *
     * @param config Javelin�ݒ�B
     *
     */
    public HadoopJavelinFileGenerator(final JavelinConfig config)
    {
    	super(config);
    }

    /**
     * Javelin���O�Ƃ��āA�t�@�C���ɏo�͂���B
     * javelin.download.max�𒴂���ꍇ�ɂ́A�������đ��M����B
     *
     *
     * @param jvnLogBuilder ���C�^�[
     * @param tree {@link CallTree}�I�u�W�F�N�g
     * @param node �m�[�h�B
     * @param endNode ���O�ɏo�͂��� CallTree �̍Ō�̃m�[�h�i���̃m�[�h�܂ŏo�͂����j
     * @param callback JavelinCallback�B
     * @param jvnFileFullPath jvn�t�@�C���̃t���p�X�B
     * @param jvnFileName jvn�t�@�C�����B
     * @param telegramId �d�� ID
     * @return ���������m�[�h���o�͂���ꍇ�� <code>true</code> �A�m�[�h�o�͂��I������ꍇ�� <code>false</code>
     */
    public static boolean generateJavelinFileImpl(final StringBuilder jvnLogBuilder,
            final CallTree tree, final CallTreeNode node, final CallTreeNode endNode,
            JavelinLogCallback callback, String jvnFileName, String jvnFileFullPath,
            final long telegramId)
    {
        if (tree instanceof HadoopCallTree)
        {
            HadoopCallTree hadoopCallTree = (HadoopCallTree) tree;
            List<CallTree> hadoopCallTreeList = hadoopCallTree.getChildren();
            for (CallTree hadoopChildCallTree : hadoopCallTreeList)
            {
                generateJavelinFileImpl(jvnLogBuilder,
                                        hadoopChildCallTree,
                                        hadoopChildCallTree.getRootNode(),
                                        null,
                                        callback,
                                        jvnFileName,
                                        jvnFileFullPath,
                                        telegramId);
            }
        }

        JavelinConfig config = new JavelinConfig();
        if (jvnLogBuilder.length() > config.getJvnDownloadMax())
        {
            flushBuffer(jvnLogBuilder, jvnFileName, jvnFileFullPath, callback,
                    config, telegramId);
        }

        if (node == null)
        {
            StackTraceElement[] stacktraces = ThreadUtil.getCurrentStackTrace();
            String stackTraceStr = "(JavelinFileGenerator#generateJavelinFileImpl) node is NULL.\n";
            stackTraceStr += ThreadUtil.getStackTrace(stacktraces, stacktraces.length);
            SystemLogger.getInstance().warn(stackTraceStr);
            return true;
        }

        // �t�@�C����1���b�Z�[�W���������ށB
        if (node.getInvocation() != null)
        {
            String jvnCallMessage = createLogMessage(tree, node);
            if (jvnCallMessage != null)
            {
                jvnLogBuilder.append(jvnCallMessage);
            }
        }

        List<CallTreeNode> children = node.getChildren();
        boolean continuePrint = true;
        for (int index = 0; index < children.size(); index++)
        {
            CallTreeNode child = children.get(index);
            continuePrint =
                    generateJavelinFileImpl(jvnLogBuilder, tree, child, endNode, callback,
                                            jvnFileName, jvnFileFullPath, telegramId);
            if (continuePrint == false || child == endNode)
            {
                continuePrint = false;
                break;
            }
        }

        // Throw���O���������ށB
        if (node.getThrowable() != null)
        {
            writeThrowLog(jvnLogBuilder, tree, node);
        }

        // Event���O���������ށB
        CommonEvent[] eventList = node.getEventList();
        if (eventList != null)
        {
            for (CommonEvent event : eventList)
            {
                writeEventLog(jvnLogBuilder, tree, node, event);
            }
        }

        String jvnReturnMessage = "";
        if (node.getEndTime() >= 0)
        {
            if (node.isFieldAccess())
            {
                jvnReturnMessage =
                        JavelinLogMaker.createJavelinLog(ID_FIELD_WRITE, node.getEndTime(), tree,
                                                         node);
            }
            else
            {
                jvnReturnMessage =
                        JavelinLogMaker.createJavelinLog(ID_RETURN, node.getEndTime(), tree, node);
            }
        }

        // �t�@�C����1���b�Z�[�W���������ށB
        if (jvnReturnMessage != null)
        {
            jvnLogBuilder.append(jvnReturnMessage);
        }

        return continuePrint;
    }

    private static void writeEventLog(StringBuilder writer, CallTree tree, CallTreeNode node,
            CommonEvent event)
    {
        String jvnThrowMessage = JavelinLogMaker.createEventLog(event, tree, node);

        // �t�@�C����1���b�Z�[�W���������ށB
        if (jvnThrowMessage != null)
        {
            writer.append(jvnThrowMessage);
        }
    }

    private static String createLogMessage(final CallTree tree, final CallTreeNode node)
    {
        String jvnCallMessage;
        if (node.isFieldAccess())
        {
            jvnCallMessage = JavelinLogMaker.createJavelinLog(ID_FIELD_READ,
                    node.getStartTime(), tree, node);
        }
        else
        {
            jvnCallMessage =
                    JavelinLogMaker.createJavelinLog(ID_CALL, node.getStartTime(), tree, node);
        }
        return jvnCallMessage;
    }

    private static void writeThrowLog(final StringBuilder writer, final CallTree tree,
            final CallTreeNode node)
    {
        String jvnThrowMessage =
                JavelinLogMaker.createJavelinLog(ID_THROW, node.getThrowTime(), tree, node);

        // �t�@�C����1���b�Z�[�W���������ށB
        if (jvnThrowMessage != null)
        {
            writer.append(jvnThrowMessage);
        }
    }

    /**
     * �o�b�t�@�̓��e��jvn���O�t�@�C���A�ʒm�Ƃ��đ��M����B
     *
     * @param builder �o�b�t�@���e�B
     * @param jvnFileName jvn�t�@�C�����B
     * @param jvnFileFullPath ��vn�t�@�C���̃t���p�X�B
     * @param callback Callback�I�u�W�F�N�g�B
     * @param telegramId �d�� ID
     * @param config �ݒ�B
     */
    static void flushBuffer(StringBuilder builder, String jvnFileName, String jvnFileFullPath,
            JavelinLogCallback callback, JavelinConfig config, long telegramId)
    {

        try
        {
            String content = builder.toString();
            if (config.isLogJvnFile())
            {
                if (jvnFileFullPath != null)
                {
                    writeToFile(jvnFileFullPath, content);
                }
            }

            if (jvnFileName != null && callback != null)
            {
                callback.execute(jvnFileName, content, telegramId);
            }
        }
        catch (Exception ex)
        {
            SystemLogger.getInstance().warn(ex);
        }

        if (builder.length() > 0)
        {
            builder.delete(0, builder.length());
        }
    }

    private static void writeToFile(final String jvnFileName, final String content)
    {
        Writer writer = null;
        try
        {
            writer = new FileWriter(jvnFileName, true);
            writer.write(content);
            writer.flush();
        }
        catch (IOException ioEx)
        {
            SystemLogger.getInstance().warn(ioEx);
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (IOException ioEx)
                {
                    SystemLogger.getInstance().warn(ioEx);
                }
            }
        }
    }

}
