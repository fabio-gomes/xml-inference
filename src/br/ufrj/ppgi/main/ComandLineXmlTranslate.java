
package br.ufrj.ppgi.main;

import java.io.File;
import java.util.HashMap;

import br.ufrj.ppgi.parser.XMLParser;

public class ComandLineXmlTranslate {
    
    public static void main(String[] args) {
        String filePath = args[0];
        File file = new File(filePath);
        if (file.exists()) {
            HashMap<String, File> fileList = new HashMap<String, File>();
            fileList.put(file.getName(), file);
            XMLParser.getInstance().setClearData(true);
            XMLParser.getInstance().setResetLastId(true);
            XMLParser.getInstance().executeParseSax(fileList);
        }
    }
}
