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
package jp.co.acroquest.endosnipe.javelin.converter.hbase.monitor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HMaster��������擾���邽�߂ɁA�ǉ������C���^�t�F�[�X�B
 * 
 * @author eriguchi
 */
public interface HMasterAccessor
{
    /**
     * Online��Ԃ̑S�T�[�o�ɂ��āA�T�[�o�����L�[�A�T�[�o����l�Ƃ���}�b�v���擾����B
     * 
     * @return �T�[�o�����L�[�A�T�[�o����l�Ƃ���}�b�v�B
     */
    Map<String, HServerInfo> getServerInfo();
    
    /**
     * Offline��Ԃ̑S�T�[�o�ɂ��āA�T�[�o���̃Z�b�g���擾����B
     * 
     * @return �T�[�o���̃Z�b�g�B
     */
    Set<String> getDeadServerInfo();
    
    /**
     * �S�e�[�u���̃��X�g���擾����B
     * 
     * @return �S�e�[�u���̃��X�g�B
     */
    List<HTableDescriptor> listTables();

    /**
     * �w�肵���e�[�u���̃��[�W���������擾����B
     * 
     * @param name �e�[�u�����B
     * @return �w�肵���e�[�u���̃��[�W�������B
     */
    int getRegionsOfTable(byte[] name);
    
    /**
     * �T�[�o�����L�[�A�����ɏ������郊�[�W�������̃��X�g��l�Ƃ���}�b�v���擾����B
     * 
     * @return �T�[�o�����L�[�A�����ɏ������郊�[�W�������̃��X�g��l�Ƃ���}�b�v�B
     */
    Map<HServerInfo, List<HRegionInfo>> getAssignments();
}
