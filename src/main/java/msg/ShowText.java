package msg;

/**
 * 一个固定格式的展示板
 */
public class ShowText {
    private  StringBuilder all=new StringBuilder();
    private  StringBuilder header1=new StringBuilder();
    private  StringBuilder header2=new StringBuilder();
    private  StringBuilder info1=new StringBuilder();
    private  StringBuilder info2=new StringBuilder();
    private  StringBuilder oneTimeInfo1=new StringBuilder();
    private  StringBuilder oneTimeInfo2=new StringBuilder();
    private  StringBuilder imgString=new StringBuilder(); 

    public StringBuilder getAll() {
        return all;
    }

    public StringBuilder getHeader1() {
        return header1;
    }

    public StringBuilder getHeader2() {
        return header2;
    }

    public StringBuilder getInfo1() {
        return info1;
    }

    public StringBuilder getInfo2() {
        return info2;
    }

    public StringBuilder getOneTimeInfo1() {
        return oneTimeInfo1;
    }

    public StringBuilder getOneTimeInfo2() {
        return oneTimeInfo2;
    }

    public void clearAll(){
        clear(header1);
        clear(header2);
        clear(info1);
        clear(info2);
        clear(oneTimeInfo1);
        clear(oneTimeInfo2);
    }

    private void clear(StringBuilder text){
        text.delete(0, text.length());
    }

    public void clearHeader1(){
        clear(header1);
    }

    public void clearHeader2(){
        clear(header2);
    }

    public void clearInfo1(){
        clear(info1);
    }

    public void clearInfo2(){
        clear(info2);
    }
    public void clearOneTimeInfo1(){
        clear(oneTimeInfo1);
    }

    public void clearOneTimeInfo2(){
        clear(oneTimeInfo2);
    }

    public  int sizeHeader1(){
        return header1.length();
    }
    public  int sizeHeader2(){
        return header2.length();
    }
    public  int sizeInfo1(){
        return info1.length();
    }
    public  int sizeInfo2(){
        return info2.length();
    }
    public  int sizeOneTimeInfo1(){
        return oneTimeInfo1.length();
    }
    public  int sizeOneTimeInfo2(){
        return oneTimeInfo2.length();
    }


    public void setHeader1(String s){
        clear(header1);
        header1.append(s);
    }

    public void setHeader2(String s){
        clear(header2);
        header2.append(s);
    }

    public void setInfo1(String s){
        clear(info1);
        info1.append(s);
    }

    public void setInfo2(String s){
        clear(info2);
        info2.append(s);
    }

    public void setOneTimeInfo1(String s){
        clear(oneTimeInfo1);
        oneTimeInfo1.append(s);
    }

    public void setOneTimeInfo2(String s){
        clear(oneTimeInfo2);
        oneTimeInfo2.append(s);
    }

    public void appendHeader1(String s){
        header1.append(s);
    }

    public void appendHeader2(String s){
        header2.append(s);
    }

    public void appendInfo1(String s){
        info1.append(s);
    }

    public void appendInfo2(String s){
        info2.append(s);
    }
    public void appendOneTimeInfo1(String s){
        oneTimeInfo1.append(s);
    }

    public void appendOneTimeInfo2(String s){
        oneTimeInfo2.append(s);
    }

    public void setImg(String fileId){
        imgString.delete(0,imgString.length());
        imgString.append(fileId);
    }

    public String getShowImg(){
        return imgString.toString();
    }

    public String toString(){
        clear(all);
        if (header1.length()>0){
            all.append(header1).append("\n");
        }
        if (header2.length()>0){
            all.append(header2).append("\n");
        }
        if (info1.length()>0){
            all.append(info1).append("\n");
        }
        if (info2.length()>0){
            all.append(info2).append("\n");
        }
        //不会被初始化，但是只显示1次
        if (oneTimeInfo1.length()>0){
            all.append(oneTimeInfo1).append("\n");
            clearOneTimeInfo1();
        }
        if (oneTimeInfo2.length()>0){
            all.append(oneTimeInfo2).append("\n");
            clearOneTimeInfo2();
        }
        return all.toString();
    }
    public String toAllPartString(){
        StringBuilder sb=new StringBuilder();
        if (header1.length()>0){
            sb.append("header1:").append(header1).append("\n");
        }
        if (header2.length()>0){
            sb.append("header2:").append(header2).append("\n");
        }
        if (info1.length()>0){
            sb.append("info1:").append(info1).append("\n");
        }
        if (info2.length()>0){
            sb.append("info2:").append(info2).append("\n");
        }
        //不会被初始化，但是只显示1次
        if (oneTimeInfo1.length()>0){
            sb.append("oneTimeInfo1:").append(oneTimeInfo1).append("\n");
             
        }
        if (oneTimeInfo2.length()>0){
            sb.append("oneTimeInfo2:").append(oneTimeInfo2).append("\n");
            
        }
        return sb.toString();
    }
}
