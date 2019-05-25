/**
 * @Description  通过网址获取网页源代码
 * @Author Maweijun
 * @Date 2019/5/22  11:09
 * @Version 1.0
 */
package com.lut.wlw.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ByUrlGetHtml {
    /**
     * @MethodName getHtml
     * @Description 通过网站获取源码
     * @Author Maweijun [url 网址, encoding 网页字符集编码]
     * @Return java.lang.String 源码字符串
     * @Date 2019/5/22 0022 11:17
     */
    public static String getHtml(String url,String encoding){
        //StringBuilder线程不安全的，适合于单线程 ，StrinBuffer 线程安全的适合于多线程
        StringBuilder sb = new StringBuilder();
        BufferedReader bf = null;
        try {
            //建立连接
            URL urlobj = new URL(url);
            //打开链接
            URLConnection uc = urlobj.openConnection();
            //建立文件输入流 字节流
            InputStream is = uc.getInputStream();
            //创建缓冲流  需要参数字符流 创建一个转换流  三个流是装饰者设计模式的实现
            //所以final中只是关闭了bf，其他流系统会自动关闭
            bf = new BufferedReader(new InputStreamReader(is,encoding));
            //每次读取一行
            String line;
            while ((line = bf.readLine())!=null){
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bf != null){
                try {
                    bf.close();
                    //可提醒gc垃圾收集
                    bf = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //返回网页内容字符串
        return sb.toString();
    }
    /**
     * @MethodName arealink
     * @Description 通过源码分析出区域链接
     * @Author Maweijun
     * @Param [html] 网页源码
     * @Return java.util.List<java.lang.String> 区域链接list集合
     * @Date 2019/5/22  12:52
     */
    public static List<String> arealink (String html){
        List<String> areaList = new ArrayList<String>();
        String areaUrl = "https://bj.lianjia.com";
        //将网页源码转换为document文档对象类似js操作dom节点的方式解析html内容
        Document document = Jsoup.parse(html);
        //取得area外围的盒子
        Element filter = document.getElementById("filter");
        //取得链接外围li
        Elements li = filter.getElementsByTag("ul").get(1).getElementsByTag("li");
        //遍历获取每一个li的<a>标签内容
        for (Element url:li) {
            String attr = url.getElementsByTag("a").attr("href");
            //过滤第一个不限项目
            if (attr.length()>8) {
                areaList.add(areaUrl+attr);
            }
        }
        return areaList;
    }
    /**
     * @MethodName pageLink
     * @Description 获取二级栏目下的所有动态分页下的全部页面URL
     * @Author Maweijun
     * @Param [areapageList] 区域
     * @Return java.util.List<java.lang.String>
     * @Date 2019/5/24  17:30
     */
    public static List<String> pageLink (List<String> areapageList){
        //存放所有区域房源页面URL
        List<String> pageList = new ArrayList<String>();
        for (String areaUrl:areapageList) {

            try {
                String areaHtml = getHtml(areaUrl, "utf-8");
                Document document = Jsoup.parse(areaHtml);
                String pageSize = document.getElementsByClass("content__pg").get(0).attr("data-totalPage");
                int size = Integer.parseInt(pageSize);
                for (int i = 0; i <size; i++) {
                    // System.out.println(areaUrl+"pg"+(i+1)+"/#contentList");
                    pageList.add(areaUrl+"pg"+(i+1)+"/#contentList");
                }

            } catch (Exception e) {
                System.out.println("该区域无房源");
            }
        }
        return pageList;
    }
    /**
     * @MethodName getHouseInfo
     * @Description  获取租赁房信息并写入文本文件中
     * @Author Maweijun
     * @Param [housePagesList]
     * @Return void
     * @Date 2019/5/25  17:35
     */
    public void getHouseInfo (List<String> housePagesList) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(new File("D:\\house\\house.txt"));
        try {
            for (int i = 0; i <housePagesList.size() ; i++) {
                for (String houseUrl:housePagesList) {
                    String houseHtml = getHtml(houseUrl, "utf-8");
                    Document houseDom = Jsoup.parse(houseHtml);
                    Elements content__list = houseDom.getElementsByClass("content__list--item");
                    for (Element houseList:content__list) {
                        String attr =  houseList.getElementsByClass("content__list--item--des").text();
                        String addr =  houseList.getElementsByClass("content__list--item--title twoline").text();
                        String price =houseList.getElementsByClass("content__list--item-price").text();
                        //System.out.println(attr+" / "+addr+" / "+price);
                        String info = attr + addr + price;
                        byte[] bytes = info.getBytes();
                        fos.write(bytes,0,bytes.length);
                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static void main(String[] args) throws FileNotFoundException {
        //获取网页源码
        String html = getHtml("https://bj.lianjia.com/zufang/","utf-8");
        //获取所有区域链接信息
        List<String> areaList = arealink(html);
        //获取每个区域每个页面链接信息，加页面数后缀的页面
        List<String> housePage = pageLink(areaList);
        //获取页面内容,注意静态和非静态方法的调用
        ByUrlGetHtml byUrlGetHtml = new ByUrlGetHtml();
        byUrlGetHtml.getHouseInfo(housePage);
        }

}
