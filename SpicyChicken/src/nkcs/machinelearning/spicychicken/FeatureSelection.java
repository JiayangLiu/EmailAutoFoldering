package nkcs.machinelearning.spicychicken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Created by FUYutong on 25/05/2017.
 */
public class FeatureSelection {

    int m_NumInstances;//数据条数
    int m_NumAttributes;//属性数
    int m_NumClasses;//用于分类的属性值个数
    double[] m_Classvalues;//分类属性每个值的个数
    double[] m_AUC;//存储除分类属性外每个属性的AUC值
    int[] m_Index;//用于存储排序后的AUC值对应属性号码
    double[] AUC;//排序后的新的AUC数组
    Attribute[] m_AttributeName; //属性集

    public Instances ChooseAttribute(Instances data, int attributeNum) throws Exception {

        int deleteNum = data.numAttributes() - 1 - attributeNum;//删除的属性的数目
        int[] delete_Index = new int[deleteNum];//存储要删除的属性号
        //System.out.println(deleteNum);
        Instances changed_Instance = new Instances(data, data.numInstances());
        //System.out.println(changed_Instance.numAttributes());

        for (int i = 0; i < m_NumInstances; i++) {
            changed_Instance.add(data.instance(i));//对实例进行分类放入不同枝干实例集中
        }

        for (int i = 0; i < deleteNum; i++) {
            delete_Index[i] = m_Index[attributeNum + i];//初始化
        }

        //下面对delete_Index降序排序
        int temp = 0;
        for (int i = 0; i < deleteNum; i++) {
            for (int j = 0; j < deleteNum - 1 - i; j++) {
                if (delete_Index[j] < delete_Index[j + 1]) {
                    temp = delete_Index[j];
                    delete_Index[j] = delete_Index[j + 1];
                    delete_Index[j + 1] = temp;
                }
            }
        }

        //下面按照delete_Index数组中值顺序删除属性
        for (int i = 0; i < deleteNum; i++) {
            changed_Instance.deleteAttributeAt(delete_Index[i]);
        }

        return changed_Instance;
    }

    public void GenerateAUC(Instances data) throws Exception {
        //只处理分类值为2的实例集，默认设置classValue(0)为阳性值，classValue(1)为阴性值

        m_NumInstances = data.numInstances();
        m_NumAttributes = data.numAttributes();
        m_NumClasses = data.numClasses();
        m_Classvalues = new double[m_NumClasses]; //存储分类属性每个值的个数
        m_AUC = new double[m_NumAttributes - 1];//存储除分类属性外的所有属性AUC值

        for (int i = 0; i < data.numInstances(); i++) {
            int m = (int) data.instance(i).classValue();
            m_Classvalues[m] += 1;
        }

        for (int i = 0; i < m_NumAttributes - 1; i++) {
            if (data.attribute(i).isNominal()) {
                m_AUC[i] = CalcuateNominalAUC(i, data);//调用计算离散型属性AUC的方法
            } else {
                m_AUC[i] = CalcuateAUC(i, data);//调用计算连续型属性AUC的方法
            }
        }

    }

    public double CalcuateNominalAUC(int m_attIndex, Instances data) throws Exception {
        //计算离散型属性的AUC值，默认设置classValue(0)为阳性值，classValue(1)为阴性值
        double m_AOC = 0.0;
        int m_NumAttributeValue = data.attribute(m_attIndex).numValues();//记录当前属性值个数
        int[] m_AttributeValues = new int[m_NumAttributeValue];//存储每个属性值对应样本个数
        int[] m_TrueClassValues = new int[m_NumAttributeValue];//存储每类属性值对应阳性分类值个数
        double[] m_Property = new double[m_NumAttributeValue];//存储阳性值的百分比
        double[] sorted_Property = new double[m_NumAttributeValue];//存储排序后的m_Property值
        int[] sorted_AttributeValues = new int[m_NumAttributeValue];//存储排序后每组属性值的样本个数
        int[] sorted_Index = new int[m_NumAttributeValue];//存储排序后的m_Property值的原属性号
        double[] m_tpr = new double[m_NumAttributeValue + 1];//真阳性比率(第一个值为0）
        double[] m_fpr = new double[m_NumAttributeValue + 1];//假阳性比率（第一个值为0）

        Instances changed_Instance = new Instances(data, data.numInstances());//根据属性评分值，对所有样本重新排序生成新的实例集

        data.sort(m_attIndex);//先对样本按照离散型属性值分组

        //下面计算每一组大小
        for (int i = 0; i < data.numInstances(); i++) {
            int m = (int) data.instance(i).value(m_attIndex);
            m_AttributeValues[m] += 1;
        }

        //下面计算每一组中阳性值个数
        int k = 0;//用于记录当前统计到的样本编号，在循环语句中递增
        for (int i = 0; i < m_NumAttributeValue; i++) {
            for (int j = 0; j < m_AttributeValues[i]; j++) {
                int m = (int) data.instance(k).classValue();
                k++;
                if (m == 0)//分类值阳性
                {
                    m_TrueClassValues[i] += 1;
                }
            }
        }

        //计算阳性值百分比（评分值）
        for (int i = 0; i < m_NumAttributeValue; i++) {
            m_Property[i] = m_AttributeValues[i]==0?0:(double) m_TrueClassValues[i] / m_AttributeValues[i];
        }

        for (int i = 0; i < m_NumAttributeValue; i++) {
            sorted_Property[i] = m_Property[i];//复制m_Property

        }

        //对sorted_Property进行升序排序
        double temp = 0.0;

        for (int i = 0; i < m_NumAttributeValue; i++) {
            for (int j = 0; j < m_NumAttributeValue - 1 - i; j++) {
                if (sorted_Property[j] > sorted_Property[j + 1]) {
                    temp = sorted_Property[j];
                    sorted_Property[j] = sorted_Property[j + 1];
                    sorted_Property[j + 1] = temp;
                }
            }
        }

        //找到排序后对应属性号
        for (int i = 0; i < m_NumAttributeValue; i++) {
            for (int j = 0; j < m_NumAttributeValue; j++) {
                if (sorted_Property[i] == m_Property[j]) {
                    sorted_Index[i] = j;
                }
            }
        }

        //生成排序后的新实例集
        for (int i = 0; i < m_NumAttributeValue; i++) {
            for (int j = 0; j < data.numInstances(); j++) {
                int m = (int) data.instance(j).value(m_attIndex);
                if (m == sorted_Index[i]) {
                    changed_Instance.add(data.instance(j));
                    sorted_AttributeValues[i]++;
                }
            }
        }

        for (int i = 1; i < m_NumAttributeValue; i++) {
            sorted_AttributeValues[i] += sorted_AttributeValues[i - 1];
        }

        //对新实例集计算当前属性的AUC值
        m_tpr[0] = 0;
        m_fpr[0] = 0;
        for (int i = 0; i < m_NumAttributeValue; i++)//共有m_NumAttributeValue组值
        {
            int num_tpr = 0;
            int num_fpr = 0;

            for (int j = 0; j < sorted_AttributeValues[i]; j++) {
                int m = (int) changed_Instance.instance(j).classValue();//每个样本的分类值

                if (m == 0)//分类值阳性
                {
                    num_tpr++;
                } else {
                    num_fpr++;
                }
            }
            m_tpr[i + 1] = m_Classvalues[0]==0?0:num_tpr / m_Classvalues[0];
            m_fpr[i + 1] = m_Classvalues[0]==0?0:num_fpr / m_Classvalues[1];
        }

        for (int i = 0; i < m_NumAttributeValue; i++) {
            m_AOC += CalcuateS(m_fpr[i], m_fpr[i + 1], m_tpr[i], m_tpr[i + 1]);    //计算AUC值
        }

        if (m_AOC > 0.5)
            return m_AOC;
        else
            return (1 - m_AOC);

    }

    public double CalcuateAUC(int m_attIndex, Instances data) throws Exception {
        //这里将实例集分成10份即设置10个阈值
        double[] temp_value = new double[m_NumInstances];
        int[] temp_classvalue = new int[m_NumInstances];
        double[] m_tpr = new double[10];//真阳性比率
        double[] m_fpr = new double[10];//假阳性比率
        double m_AOC = 0.0;

        data.sort(m_attIndex);//在计算前先排序（从小到大）
        for (int i = 0; i < m_NumInstances; i++) {
            temp_value[i] = data.instance(i).value(m_attIndex);//当前实例在当前选择属性的值(连续型）
            temp_classvalue[i] = (int) data.instance(i).classValue();//当前实例的分类值下标
        }

        int size = data.numInstances() / 10;//桶大小
        for (int i = 0; i < 9; i++) {
            int num_tpr = 0;//真阳性数目
            int num_fpr = 0;//实际为阴性，错判为阳性数目
            double sum_attribute = 0.0;//属性值之和
            double threshold = 0.0;//平均属性值（阈值）

            //计算阈值
            for (int k = 0; k < size; k++) {
                int index = i * size + k;
                sum_attribute += temp_value[index];
            }
            threshold = size==0?0:sum_attribute / size;

            //计算tpr、fpr
            for (int j = 0; j < m_NumInstances; j++) {
                if (data.instance(j).value(m_attIndex) < threshold) //如果当前样本属性值比阈值小
                {
                    if (temp_classvalue[j] == 0) {
                        num_tpr++;
                    } else {
                        num_fpr++;
                    }
                }
            }

            // there is a bug: while, we need to prevent divisor to be 0, but when using "m_Classvalues[1]==0?0:" below,
            // it will say: IllegalArgumentException: Can't delete class attribute
            m_tpr[i] = num_tpr / m_Classvalues[0];
            m_fpr[i] = m_Classvalues[1]==0?0:num_fpr / m_Classvalues[1];
        }

        m_tpr[9] = 1.0;//第10个值
        m_fpr[9] = 1.0;

        //根据已有的m_tpr与m_fpr计算AOC（共11个点，包括（0,0)与(1,1))
        m_AOC += CalcuateS(0, m_fpr[0], 0, m_tpr[0]);//第一个点与原点的面积
        for (int i = 0; i < 9; i++) {
            m_AOC += CalcuateS(m_fpr[i], m_fpr[i + 1], m_tpr[i], m_tpr[i + 1]);
        }

        if (m_AOC > 0.5)
            return m_AOC;
        else
            return (1 - m_AOC);
    }

    public double CalcuateS(double x1, double x2, double y1, double y2) throws Exception {
        double S = 0;
        S = (y2 + y1) * (x2 - x1) / 2;
        return S;
    }

    public void PrintAUC(Instances data) throws Exception {
        GenerateAUC(data);//调用GerateAUC 生成AUC值
        //降序排序
        double temp = 0.0;
        AUC = new double[m_NumAttributes - 1];//排序后的新的数组
        m_Index = new int[m_NumAttributes - 1];//用于存储排序后的AUC值对应属性号码
        m_AttributeName = new Attribute[m_NumAttributes - 1];//用于存储排序后的AUC值对应属性名称

        for (int i = 0; i < m_NumAttributes - 1; i++) {
            AUC[i] = m_AUC[i];//复制m_AUC
        }

        for (int i = 0; i < m_NumAttributes - 1; i++) {
            for (int j = 0; j < m_NumAttributes - 2 - i; j++) {
                if (AUC[j] < AUC[j + 1]) {
                    temp = AUC[j];
                    AUC[j] = AUC[j + 1];
                    AUC[j + 1] = temp;
                }
            }
        }

        for (int i = 0; i < m_NumAttributes - 1; i++) {
            for (int j = 0; j < m_NumAttributes - 1; j++) {
                if (AUC[i] == m_AUC[j]) {
                    m_Index[i] = j;
                    m_AttributeName[i] = data.attribute(j);
                }
            }
        }

//        System.out.println("按AUC值的大小降序排序：(属性号从0开始）");
//        for (int i = 0; i < m_NumAttributes - 1; i++) {
//            System.out.println();
//            System.out.println("属性" + m_Index[i] + "  " + "名字: " + data.attribute(m_Index[i]) + "  " + "AUC值: " + AUC[i]);
//        }
    }

    public void filtrateAttribute(String fieldName, int attributeNumber) throws Exception {
        File projectDirectory = new File("");
        String resultPath = projectDirectory.getCanonicalPath();
        DataSource source = new DataSource(resultPath + "/result/fw_" + fieldName + ".arff");
        Instances data = source.getDataSet();
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        data.deleteWithMissingClass();

        //输出AUC值
        PrintAUC(data);

        Instances changed_Instances = ChooseAttribute(data, attributeNumber);

        File file = new File(resultPath + "/result/fs_" + fieldName + ".arff");
        // create it if file doesn't exists
        if (!file.exists())
            file.createNewFile();
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(changed_Instances.toString());
        bw.close();
        System.out.println("Export to fs_" + fieldName + ".arff done!");
    }
}
