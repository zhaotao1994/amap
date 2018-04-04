package vip.zhaotao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import vip.zhaotao.util.HttpClientUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @title 行政区
 * @description 获取高德地图中国行政区信息
 */
public class District {

    /**
     * @param jsonObject
     * @return String
     * @title 拼装（国级别）
     * @description
     */
    public static String assembly(JSONObject jsonObject) {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO district(adcode,citycode,name,longitude,latitude,level,pcode) VALUES('").append(jsonObject.getString("adcode")).append("',NULL,");
        sb.append("'").append(jsonObject.getString("name")).append("',");
        sb.append(jsonObject.getString("center")).append(",");
        sb.append("'").append(jsonObject.getString("level")).append("',NULL);\n");
        return sb.toString();
    }

    /**
     * @param str
     * @return String
     * @title 处理空
     * @description
     */
    private static String handleEmpty(String str) {
        String result = "NULL";
        if (!StringUtils.equals(str, "[]")) {
            result = str;
        }
        return result;
    }

    /**
     * @param jsonObject
     * @param pcode      父地址编码
     * @return String
     * @title 拼装（省、市、区级别）
     * @description
     */
    public static String assembly(JSONObject jsonObject, String pcode) {
        StringBuffer sb = new StringBuffer();
        // 区域编码
        sb.append("INSERT INTO district(adcode,citycode,name,longitude,latitude,level,pcode) VALUES('").append(jsonObject.getString("adcode")).append("',");
        // 城市编码（区号）
        sb.append("'").append(handleEmpty(jsonObject.getString("citycode"))).append("',");
        // 行政区名称
        sb.append("'").append(jsonObject.getString("name")).append("',");
        // 经纬度
        sb.append(jsonObject.getString("center")).append(",");
        // 级别 : country(国家), province(省), city(市), district(区), biz_area(商圈),
        // street(街道)
        sb.append("'").append(jsonObject.getString("level")).append("',");
        // 父地址编码
        sb.append("'").append(pcode).append("');\n");
        return sb.toString();
    }

    /**
     * @param path
     * @param content
     * @return void
     * @title 保存至文件
     * @description
     */
    public static void saveFile(String path, String content) {
        FileOutputStream fos = null;
        File file = new File(path);
        try {
            // 不存在则创建
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成建表sql
     *
     * @return
     */
    public static String generateSql() {
        StringBuilder content = new StringBuilder();
        content.append("-- MySQL").append("\n");
        content.append("DROP TABLE IF EXISTS district;").append("\n");
        content.append("CREATE TABLE district (").append("\n");
        content.append("\tadcode varchar(64) NULL,").append("\n");
        content.append("\tcitycode varchar(64) NULL,").append("\n");
        content.append("\tname varchar(64) NULL,").append("\n");
        content.append("\tlongitude double NULL,").append("\n");
        content.append("\tlatitude double NULL,").append("\n");
        content.append("\t`level` varchar(64) NULL,").append("\n");
        content.append("\tpcode varchar(64) NULL").append("\n");
        content.append(");");

        content.append("\n\n\n");

        content.append("-- Oracle").append("\n");
        content.append("CREATE TABLE \"DISTRICT\" (").append("\n");
        content.append("\t\"ADCODE\" VARCHAR2(64),").append("\n");
        content.append("\t\"CITYCODE\" VARCHAR2(64),").append("\n");
        content.append("\t\"NAME\" VARCHAR2(64),").append("\n");
        content.append("\t\"LONGITUDE\" NUMBER,").append("\n");
        content.append("\t\"LATITUDE\" NUMBER,").append("\n");
        content.append("\t\"LEVEL\" VARCHAR2(64),").append("\n");
        content.append("\t\"PCODE\" VARCHAR2(64)").append("\n");
        content.append(");");

        content.append("\n\n\n");
        return content.toString();
    }

    public static void main(String[] args) {
        String amapKey = "";
        if (StringUtils.isBlank(amapKey)) {
            System.err.println(("请设置高德Key！详情参考：https://lbs.amap.com/api/webservice/guide/create-project/get-key"));
            return;
        }

        try {
            System.err.println("获取省市区中。。。");
            long begin = System.currentTimeMillis();

            // 目标文件文件夹
            String targetFileFolder = System.getProperty("user.home");
            // 系统名称，如为Windows则将目标文件生成至Desktop文件夹中，即桌面
            String osName = System.getProperty("os.name");
            boolean isWindows = osName.contains("Windows");
            if (isWindows) {
                targetFileFolder = String.format("%s/%s", targetFileFolder, "Desktop/");
            }
            // 目标文件名称
            String targetFileName = String.format("%s_%d.sql", "district", System.currentTimeMillis());
            // 目标文件路径
            String targetFilePath = String.format("%s/%s", targetFileFolder, targetFileName);

            // 请求URL
            String url = "http://restapi.amap.com/v3/config/district";
            // 请求参数
            Map<String, String> params = new HashMap<String, String>();
            // 高德Key
            params.put("key", "96f7910fe34fa34bf5f976b3e9ebe87c");
            // 设置显示下级行政区级数，可选值：0、1、2、3
            params.put("subdistrict", "3");
            // 发送请求
            JSONObject jsonObject = JSON.parseObject(HttpClientUtil.sendPost(url, params));

            // 行政区内容
            StringBuilder content = new StringBuilder();
            content.append(generateSql());

            // status : 0表示失败、1表示成功
            if (jsonObject != null && StringUtils.equals(jsonObject.getString("status"), "1")) {
                // 国家
                JSONObject country = jsonObject.getJSONArray("districts").getJSONObject(0);
                content.append(assembly(country));
                // 省、市、区JSON数组
                JSONArray provinceArray, cityArray, districtArray;
                provinceArray = country.getJSONArray("districts");
                // 省、市JSON对象
                JSONObject province, city, district;
                // 遍历省级
                for (int i = 0; i < provinceArray.size(); i++) {
                    province = provinceArray.getJSONObject(i);
                    content.append(assembly(province, country.getString("adcode")));
                    cityArray = province.getJSONArray("districts");
                    // 遍历市级
                    for (int j = 0; j < cityArray.size(); j++) {
                        city = cityArray.getJSONObject(j);
                        content.append(assembly(city, province.getString("adcode")));
                        districtArray = city.getJSONArray("districts");
                        // 遍历区级
                        for (int k = 0; k < districtArray.size(); k++) {
                            district = districtArray.getJSONObject(k);
                            content.append(assembly(district, city.getString("adcode")));
                        }
                    }
                }
                // 保存至文件
                saveFile(targetFilePath, content.toString());
                // 打开文件
                Runtime.getRuntime().exec(String.format("%s %s", isWindows ? "cmd /c start" : "vi", targetFilePath));
                System.err.println(String.format("获取完毕！耗时%d毫秒！", System.currentTimeMillis() - begin));
            } else {
                System.err.println("获取失败！");
                System.err.println(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
