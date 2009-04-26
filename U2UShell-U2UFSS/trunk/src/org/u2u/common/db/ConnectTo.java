/*
 * ConnectTo.java
 *
 * Created on 20 de abril de 2007, 20:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.u2u.common.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.u2u.common.data.Matrix;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.Calendar;
import java.util.GregorianCalendar;



/**
 * @author sergio
 */

public class ConnectTo
{
    //Variables de Instancia
    /** variable que contiene la conexion*/
    private Connection con; 
    /** variable que apunta a un resultset*/
    private ResultSet rs;
    /** variable que apunta a un resultsetMetadata para metadatos*/
    private ResultSetMetaData rsM;
    /** variable que apunta a un statement*/
    private Statement stmt;
    
    //Metodos de instancia
    /** Crea una nueva instancia de ConnectTo y registra el driver de la db en el DriverManager 
     * @param driver 
     */
    public ConnectTo(String driver)
    {
        try
        {
            Class.forName(driver).newInstance();
        }
        catch(Exception ex)
        {
           System.out.println(ex.getMessage());
        }
    }

    private Statement createStatement()
    {
        //creando un Statements para ejecutar SQL
        
        try
        {        
            return con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY);
        } 
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    /**
     * Return a prepared statements for executing special querys
     * @param sql
     * @return
     */
    public PreparedStatement createPreparedStatement(String sql)
    {
        PreparedStatement pstmt = null;
        
        try {
            pstmt = con.prepareStatement(sql);
        } catch (SQLException ex) {
            Logger.getLogger(ConnectTo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return pstmt;
    }
    
    /** Ejecuta una instruccion de sql cualesquiera(update, insert,...) diferente de select
     * @param consulta string de consulta, contiene instrucciones de sql
     * @return devuelve si se pudo o no hacer la consulta
     */
    public boolean sQLExecute(String consulta)
    {
        //creando Statement
        stmt = this.createStatement();
        
        //ejecutando consulta
        try
        {
            int status = stmt.executeUpdate(consulta);
            if(status>0)
                return true;
            else
                return false;
            
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
            return false;
        }
        
        
        
    }

    /** Ejecuta una instruccion de sql cualesquiera(update, insert,...) diferente de select
     * @param consulta string de consulta, contiene instrucciones de sql
     * @return devuelve el numero de filas afectadas al hacer la consulta
     */
    public int sQLExecuteUpdate(String consulta)
    {
        //creando Statement
        stmt = this.createStatement();

        //ejecutando consulta
        try
        {
            return stmt.executeUpdate(consulta);
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
            return 0;
        }
    }
    
    /** Ejecuta una instruccion select de sql y devuelve un ResultSet
     * @param consulta string de consulta, contiene instrucciones de sql
     * @return ResultSet
     */
    public ResultSet sQLQuery(String consulta)
    {
        //creando Statement
        stmt = this.createStatement();
        
        //creando ResultSet para guardar la consulta 
        /*ResultSet*/ 
        rs = null;
        try
        {
            rs = stmt.executeQuery(consulta);
            rs.setFetchDirection(ResultSet.CONCUR_READ_ONLY);
            //rs.first();
            rsM = null;
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        
        return rs; 
    }
    
  
     /** Ejecuta una instruccion select de sql y devuelve una Matrix con su contenido
     * @param consulta string de consulta, contiene instrucciones de sql
     * @return devuelve una Matrix
     */
    public Matrix sQLQueryMatrix(String consulta)
    {
        //creando Statement
        stmt = this.createStatement();
        
        //creando ResultSet para guardar la consulta 
        Matrix mt = null;
        try
        {
            rs = stmt.executeQuery(consulta);
            mt = new Matrix(rs);
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        
        return mt;
    }
    
     /** Ejecuta una instruccion select de sql y devuelve una Matrix con su contenido
     * @param consulta string de consulta, contiene instrucciones de sql
     */
    public String[][] sQLQueryArrayString(String consulta)
    {
        //creando Statement
        stmt = this.createStatement();
        //creando ResultSet para guardar la consulta 
        String[][] mt = null;
        int r, c;//r -> filas y c -> columnas
        try
        {
            rs = stmt.executeQuery(consulta);
        
            //rows
            rs.last();
            r = rs.getRow();
            //cols
            c = rs.getMetaData().getColumnCount();
            
            //creamos la matriz con el tamaño del resultset
            mt = new String[r][c];
            //llenamos la matriz
            for(int i=1; i<=r; i++)
            {
                rs.absolute(i);
                for(int j=1; j<=c; j++)
                    mt[i-1][j-1] = rs.getString(j);       
            }
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
        
        return mt;
    }
    
    /** Obtiene una conexiÔøΩn utilizando el DriverManager, Nota: el host es de la forma "jdbc:<manejador>://url"
     * @param host 
     * @param user 
     * @param password 
     * @return se realizo o no la conexion
     */
    public boolean getConnection(String host, String user, String password)
    {        
        
        boolean st = true;
              
        try 
        {
             con = DriverManager.getConnection(host, user, password);
        } 
        catch (SQLException ex) 
        {
            st = false;
            System.out.println(ex.getMessage());
        }
               
        return st;
    }
    
    /** Obtiene el Metadato del resultset 
     * @return variable que apunta a un ResultSetMetadata
     */
    public ResultSetMetaData getResultSetMetaData()
    {
        ResultSetMetaData rsMeta = null;
        
        if(rs!=null)
            try
            {
                rsM = rs.getMetaData();
            }catch(SQLException e) 
            {
                System.out.println(e.getMessage());
            }
            
        return rsMeta;
    }
    
    /** Obtiene el numero de registros actualizados en la consulta update*/
    public int getUpdateCount()
    {
        int c = 0;//cuenta
        
        try
        {
            c = stmt.getUpdateCount();
        }
        catch(SQLException e)
        {
            System.out.println(e.getMessage());
        }
        
        return c;        
    }
    
    /** Retorna un ArrayList con los nombres de los campos seleccionados en la consulta
     * 
     * @return 
     */
    public String[] getCellsName()
    {
        String array[] = null;
        
        if(rs!=null)
        {
            //creamos un Array de Strings pra contener los nombres en la bd de los campos de la tabla
            int end = 0;
            
            if(rsM==null)
                rsM = this.getResultSetMetaData();
            
            try
            {
                end = rsM.getColumnCount();
            }
            catch(SQLException e) 
            {
                System.out.println(e.getMessage());
            }
            
            array = new String[end];
        
            for(int i=0; i<end; i++)
            {   
                try
                {
                    array[i] = rsM.getColumnName(i);
                }
                catch(SQLException e) 
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        
        return array;
        
    }
    
    /** Retorna un ArrayList con los nombres de los campos seleccionados en la consulta
     * 
     * @return 
     */
    public int[] getCellsType()
    {
        int array[] = null;
        
        if(rs!=null)
        {
            //creamos un Array de enteros pra contener los nombres en la bd de los campos de la tabla
            int end = 0;
            
            //if(rsM==null)
                //rsM = this.getResultSetMetaData();
            
            
            try
            {
                //obtenemos los metadatos del resultset
                rsM = rs.getMetaData();
                end = rsM.getColumnCount();
                array = new int[end];
            
                for(int i=0; i<end; i++)
                    array[i] = rsM.getColumnType(i+1);

            }
            catch(SQLException e) 
            {
                System.out.println(e.getMessage());
            }
        
            
        }
        
        return array;
        
    }
    
    
    /** retorna el nombre de la tabla en el String consulta
     * @param consulta consulta de tipo select
     * @return nombre de la tabla
     */
    public static String getTableName(String consulta)
    {
        String nam = null;
        if(consulta!=null)
        {
            int ini = consulta.indexOf("from ");
            int end = consulta.indexOf(" ", ini+5);
            nam = consulta.substring(ini+5, end);
        }
        
        return nam;
    }
    
    /** retorna el campo primary key de la tabla NN, se asume que debe haber una conexion a una Db y que 
     la tabla que se pasa como parametro pertenece a esta Db*/
    public String getPrimaryKey(String table)
    {
        //miramos que la conexion a la Db este establecida
        if(con!=null)
        {
            try
            {
                //recuperamos los metadatos de la Db
                DatabaseMetaData dM = con.getMetaData();
                //obtenemos el resulset
                ResultSet pk = dM.getPrimaryKeys(null, null, table);
                pk.first();
                
                /*while(!pk.isAfterLast())
                {
                    System.out.println(j+1);
                    for(int i = 1; i<=6; i++)
                        System.out.print(i+"="+pk.getString(i)+" ");
                    pk.next();
                    System.out.println();
                    
                }*/
                
                return pk.getString(4);
                        
            }
            catch(SQLException e)
            {
                System.out.println(e.getMessage());
            }
        }
        
        return "null";
    }

    /** Obtiene la conexion primitiva que hace la clase con jdbc, es decir, retorna la instancia de Connection
     * contiene la clase ConnectTo 
     * @return devuelve la instancia de Connection
     */
    public Connection getCon()
    {
        return this.con;
    }

    /** Cierra la conexion
 
     */
    public void closeConnection()
    {
        rs = null;
        try {
            if(!con.isClosed())
                con.close();
        } 
        catch (SQLException ex) 
        {
            System.out.println(ex.getMessage());
        }
    }
    
    /** Metodo de clase para cerrar el ResultSet y a la ves al Statement
     * 
     * @param r resultset a cerrar
     */
    public static void closeResultSet(ResultSet r)
    {
        if(r!=null)
            try {
                r.close();
                r.getStatement().close();
            } 
            catch (SQLException ex) 
            {
                System.out.println(ex.getMessage());
            }
    }
        
}
