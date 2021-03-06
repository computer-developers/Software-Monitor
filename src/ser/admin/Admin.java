/*
 * Copyright 2017 Neel Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ser.admin;

import com.dataBean.IntDataBean;
import com.net.remSer.IntMainSer;
import com.net.remSer.MainSer;
import com.net.remSer.MainSerHandle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import ser.UrlTools;
import static ser.UrlTools.registerObj;
import static ser.config.Configuration.setDefaultLogDir;
import ser.database.DBCon;
import ser.db.IntDataBase;
import ser.keyGen.KeyGen;
import ser.logger.IntLogger;
import ser.logger.MyLog;
import ser.ui.IntUI;

/**
 *
 * @author Neel Patel
 */
public class Admin implements IntAdmin{
    private Scanner sc=new Scanner(System.in);
    private final IntDataBase ld;
    private final IntDataBase db;
    private List<String> auth;
    private IntUI ui;
    private String u="";
    
    public Admin(IntDataBase ld){
        this.ld=ld;
        this.db=null;
    }
    
    public Admin(IntDataBase ld,IntDataBase db){
        this.ld=ld;
        this.db=db;
    }
    
    @Override
    public void startSer() {
        try{
            Path pt=Paths.get("temp","log").toAbsolutePath();
            Files.createDirectories(pt);
            setDefaultLogDir(pt);
            //System.out.println("path - "+pt);
            //MyLog.SetDir(pt);
            MyLog mg=new MyLog();
            MainSerHandle mh=new MainSerHandle();
            mh.setKeysp(KeyGen::getKey);
            mh.setLoger(mg::log);
            try{
                if(db!=null)
                    mh.setLoger((IntLogger)db);
            }catch(Exception ex){
                
            }
            IntMainSer ms=new MainSer(mh);
            try{
                Registry r=LocateRegistry.createRegistry(8686);
            }catch(Exception e){
                System.err.println("registry fail");
            }
            String uri=registerObj(ms,8686,"main");
            if(ui!=null)
                ui.showMessage("uri :- "+uri);
            u=uri;
        }catch(Exception ex){
            System.err.println("error1 :- "+ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void stopSer() {
        try{
            UrlTools.unregister(u);
            u="";
            System.gc();
            ui.showMessage("server stoped.");
            System.out.println("list :- "+Thread.getAllStackTraces());
        }catch(Exception ex){
            System.err.println("error :- "+ex);
        }
    }

    @Override
    public List<String> getAllUname(LocalDate dt) {
        if(db!=null)
            return db.getAllUserName(dt);
        return ld.getAllUserName(dt);
    }

    @Override
    public List<IntDataBean> getUserDetail(String uName, LocalDate dt) {
        if(db!=null)
            return db.getUserDetail(uName, dt, dt.plusDays(1));
        return ld.getUserDetail(uName, dt, dt.plusDays(1));
    }

    @Override
    public boolean registerUI(IntUI ui) {
        this.ui=ui;
        return true;
    }

    @Override
    public void condb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remdb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void close()throws IOException{
        System.exit(0);
    }

    @Override
    public List<String> getAuthorized() {
        if(auth!=null)
            return Collections.unmodifiableList(auth);
        else{
            auth=readAuth();
            if(auth!=null)
                return auth;
            else{
                auth=getAuth();
                if(auth!=null){
                    writeAuth(auth);
                    return auth;
                }
                else{
                    return new ArrayList<>();
                }
            }
        }
    }
    
    private List<String> readAuth(){
        try {
            Path p=Paths.get(System.getenv("appdata"),"Software Monitor","auth.txt");
            return Files.readAllLines(p);
        } catch(Exception ex) {
            return null;
        }
    }
    
    private List<String> getAuth(){
        if(db!=null)
            return ((DBCon)db).getAuth();
        return null;
    }
    
    private boolean writeAuth(List<String> aut){
        try {
            Path p=Paths.get(System.getenv("appdata"),"Software Monitor","auth.txt");
            Files.write(p, aut);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
}
