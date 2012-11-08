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
package jp.co.acroquest.endosnipe.javelin.converter.hadoop;

import jp.co.acroquest.endosnipe.javelin.CallTreeNode;

public class HadoopCallTreeNode extends CallTreeNode {

    /** Hadoop��TaskTracker��� */
    private HadoopInfo hadoopInfo_ = null;

    /**
     * TaskTracker�̃X�e�[�^�X���������Ă��邩��Ԃ��B
     *
     * @return {@code true}�F�����Ă���^{@code false}�F�����Ă��Ȃ�
     */
    public boolean hasHadoopInfo()
    {
        return hadoopInfo_ != null;
    }

    /**
     * TaskTracker�̃X�e�[�^�X�����擾����B
     *
     * @return TaskTracker�̃X�e�[�^�X���
     */
    public HadoopInfo getHadoopInfo()
    {
        return this.hadoopInfo_;
    }

    /**
     * TaskTracker�̃X�e�[�^�X����ݒ肷��B
     *
     * @param hadoopInfo TaskTracker�̃X�e�[�^�X���
     */
    public void setHadoopInfo(HadoopInfo hadoopInfo)
    {
        this.hadoopInfo_ = hadoopInfo;
    }

    /**
     * CallTreeNode�̐e���擾����B
     * @return CallTreeNode�̐e
     */
    public HadoopCallTreeNode getParent()
    {
        return (HadoopCallTreeNode) super.getParent();
    }
}
