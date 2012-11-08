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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jp.co.acroquest.endosnipe.javelin.util.ArrayList;

/**
 * @author ochiai
 *
 */
public class HadoopMeasurementInfo
{
    /** HadoopMeasurementInfo �̗B��̃C���X�^���X */
    private static HadoopMeasurementInfo instance__ = new HadoopMeasurementInfo();

    /** �v���p�̌Â��f�[�^�������܂ł̎��ԁi�~���b�j */
    private static final int MEASUREMENT_DATA_CONSERVATION_PERIOD = 30000;

    /** �v���p�ɕۑ�����W���u�̃��X�g */
    private List<HadoopJobStatusInfo> jobStatusList_     =
                       Collections.synchronizedList(new ArrayList<HadoopJobStatusInfo>());

    /** �v���p�ɕۑ�����^�X�N���s�̃��X�g */
    private List<HadoopInfo>              taskTrackerStatusList_    =
                       Collections.synchronizedList(new ArrayList<HadoopInfo>());
    
    /** FSNamesystem�ւ̃A�N�Z�T */
    private FSNamesystemAccessor         fsNamesystem_;

    /**
     * �R���X�g���N�^���B������B
     */
    private HadoopMeasurementInfo()
    {
        // Do Nothing
    }
    
    /**
     * HadoopMeasurementInfo �̃C���X�^���X���擾����B
     * @return HadoopMeasurementInfo �̃C���X�^���X��Ԃ��B
     */
    public static HadoopMeasurementInfo getInstance()
    {
        return instance__;
    }
    
    /**
     * taskTrackerStatusList_ �ɁA�Â��f�[�^���폜���Ă���f�[�^������
     * @param hadoopInfo list�ɉ�����f�[�^
     */
    public void addToTaskTrackerStatusList(HadoopInfo hadoopInfo)
    {
        synchronized (this.taskTrackerStatusList_)
        {
            Iterator<HadoopInfo> iter = this.taskTrackerStatusList_.iterator();
            long currentTime = System.currentTimeMillis();
            while (iter.hasNext())
            {
                HadoopInfo info = iter.next();
                if (info.getTimestamp() < currentTime - MEASUREMENT_DATA_CONSERVATION_PERIOD)
                {
                    iter.remove();
                }
            }
            
            this.taskTrackerStatusList_.add(hadoopInfo);
        }
    }
    
    /**
     * taskTrackerStatusList_ ����f�[�^���擾���āA�f�[�^���폜����
     * @return taskTrackerStatusList__����擾�����f�[�^
     */
    public List<HadoopInfo> getAllTaskTrackerStatusList()
    {
        List<HadoopInfo> removedList = new ArrayList<HadoopInfo>();
        synchronized (this.taskTrackerStatusList_)
        {
            for (HadoopInfo hadoopInfo : this.taskTrackerStatusList_)
            {
                removedList.add(hadoopInfo);
            }
            this.taskTrackerStatusList_.clear();
        }
        return removedList;
    }
    
    /**
     * jobStatusList_ �ɁA�Â��f�[�^���폜���Ă���f�[�^������
     * @param jobStatusInfo list�ɉ�����f�[�^
     */
    public void addToJobStatusList(HadoopJobStatusInfo jobStatusInfo)
    {
        synchronized (this.jobStatusList_)
        {
            Iterator<HadoopJobStatusInfo> iter = this.jobStatusList_.iterator();
            long currentTime = System.currentTimeMillis();
            while (iter.hasNext())
            {
                HadoopJobStatusInfo info = iter.next();
                if (info.getTimestamp() < currentTime - MEASUREMENT_DATA_CONSERVATION_PERIOD)
                {
                    iter.remove();
                }
            }
            
            this.jobStatusList_.add(jobStatusInfo);
        }
    }
    
    /**
     * jobStatusList__ ����f�[�^���擾���āA�f�[�^���폜����
     * @return jobStatusList__����擾�����f�[�^
     */
    public List<HadoopJobStatusInfo> getAllJobStatusList()
    {
        List<HadoopJobStatusInfo> removedList = new ArrayList<HadoopJobStatusInfo>();
        synchronized (this.jobStatusList_)
        {
            for (HadoopJobStatusInfo jobInfo : this.jobStatusList_)
            {
                removedList.add(jobInfo);
            }
            this.jobStatusList_.clear();
        }
        return removedList;
    }

    /**
     * FSNameSystem�ւ̃A�N�Z�T���擾����B
     * 
     * @return FSNameSystem�ւ̃A�N�Z�T�B
     */
    public FSNamesystemAccessor getFsNamesystem()
    {
        return fsNamesystem_;
    }

    /**
     * FSNameSystem�ւ̃A�N�Z�T���擾����B
     * 
     * @param fsNamesystem FSNameSystem�ւ̃A�N�Z�T�B
     */
    public void setFsNamesystem(FSNamesystemAccessor fsNamesystem)
    {
        fsNamesystem_ = fsNamesystem;
    }
    
    /**
     * ���b�N���܂ރp�X�ɕϊ�����B
     * 
     * @param names DNS���AIP�A�h���X
     * @return ���b�N���܂ރp�X�B
     */
    public List<String> resolve(List<String> names)
    {
        if (fsNamesystem_ != null)
        {
            return this.fsNamesystem_.resolve(names);
        }
        else
        {
            return new ArrayList<String>(names);
        }
    }
}
