/*
 * Matrix.java
 * 
 * Created on 28-oct-2007, 18:59:40
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.u2u.common.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Sergio
 * Clase que encapsula un vector de vectores en su forma ArrayList de Java para simular una Matriz, dando el soporte
 * necesario para manejar la abstraccion, Nota: Verificar que el numero de columnas insertado sea el mismo 
 */
public class Matrix
{

    //Variables de Instancia
    private ArrayList matrix;//variable que representa la matrix
    //metodos de instancia
    
    /** Crea una nueva instancia de Matrix a partir de un ResultSet(JDBC)
     * @param rs resultset
     */
    public Matrix(ResultSet rs) 
    {
        matrix = new ArrayList();
        this.addRows(rs);
    }
    /** Crea una nueva instancia de Matrix a partir de un ResultSet(JDBC)*/
    public Matrix()
    {
        matrix = new ArrayList();
    }
    
    /** Añade al final de la  Matriz filas a partir de la informacion contenida en un 
     * ResultSet*  
     * @param rs 
     * @return 
     */
    //metodo 0
    public boolean addRows(ResultSet rs)
    {
        boolean st = false;
        
        //verificamos la existencia de matriz en RAM
        if(matrix!=null && rs!=null)
        {
            int numCols, i;
            
            //nos posicionamos en el primer registro del ResultSet, obtenemos el 
            // numero de columnas e empesamos a iterar llenado la MAtriz
            
            try
            {
                //
                rs.first();
                //
                numCols = (rs.getMetaData()).getColumnCount();
                //
                while(!rs.isAfterLast())
                {
                    ArrayList tmp = new ArrayList();
                                              
                    for(i=1;i<=numCols;i++)
                        tmp.add(rs.getObject(i));
                    
                    matrix.add(tmp);  
                    
                    rs.next();
                }
                st = true;
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
            }
            
            //AgathaDB.ConnectTo.closeResultSet(rs);
        }
        
        return st;
    }
    
    /** Añade al final de la  Matriz filas a partir de la informacion contenida en otra
     * Matrix*  
     * @param m 
     * @return 
     */
    //metodo 1
    public boolean addRows(Matrix m)
    {
        boolean st = false;
        //verificamos la existencia de matriz en RAM
        if(matrix!=null && m!=null)
        {
            int i, numRows;
            numRows = m.getNumRows();
            for(i=0; i<numRows; i++)
                this.addRow(m.getRow(i));
        }
        
        return st;
    }
    
    /** agrega una fila a la matriz a partir de un ArrayList
     * @param aL 
     * @return 
     */
    public boolean addRow(ArrayList aL)
    {
        boolean st = false;
        //verificamos la existencia de matriz en RAM
        if(matrix!=null && aL!=null)
        {
            matrix.add(aL);
        }
        
        return st;
    }
    
    /** Añade al a la derecha de la  Matriz columnas a partir de la informacion contenida en un 
     * ResultSet*  
     * @param rs 
     * @return 
     */
    public boolean addCols(ResultSet rs)
    {
        boolean st = false;
        
        //verificamos la existencia de matriz en RAM
        if(matrix!=null && rs!=null)
        {
            int numCols, numRows, i, j;
            
            //nos posicionamos en el primer registro del ResultSet, obtenemos el 
            // numero de columnas e empesamos a iterar llenado la MAtriz
            
            try
            {
                //
                rs.first();
                //
                numCols = (rs.getMetaData()).getColumnCount();
                
                rs.last();
                numRows = rs.getRow();
                
                //preguntamos si ya se especificaron las filas(la idea es llenarlo de izq a derecha y no de arriba a abajo)
                if(matrix.size() == 0)
                {
                    //creamos un arrayList por cada fila, en total quedarian tantos arrayList en matrix como cantida de filas(numRows) en el ResultSet
                    for(i=1; i<=numRows; i++)
                        matrix.add(new ArrayList());    
                }
                
                //llenamos de izq a derecha
                for(i=1; i<=numRows; i++)
                {
                    rs.absolute(i);
                    ArrayList tmp = (ArrayList)matrix.get(i-1);
                    for(j=1; j<=numCols; j++)
                    {
                        tmp.add(rs.getObject(j));
                    }    
                }

                st = true;
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
            }
            
            //AgathaDB.ConnectTo.closeResultSet(rs);
        }
        
        return st;
    }
    
    /** Obtener una fila de la matriz
     * @param row basado en 0, es decir, si se necesita la fila i, entonces la posicion de esta es i-1 
     * @return 
     */
    public ArrayList getRow(int row)
    {
        ArrayList aL = null;
        
        if(matrix!=null)
            aL = (ArrayList) matrix.get(row);
        
        return aL;
    }
    /** Obtener una celda i,j de la matriz
     * @param row basado en 0, es decir, si se necesita la fila i, entonces la posicion de esta es i-1 
     * @param col basado en 0, es decir, si se necesita la columna j, entonces la posicion de esta es j-1
     * @return 
     */
    public Object getCell(int row, int col)
    {
        Object obj = null;
        
        obj = (this.getRow(row)).get(col);
        
        return obj;
    }
    /** Obtener el numero de filas en la matriz
     * @return 
     */
    public int getNumRows()
    {
        return matrix.size();
    }
    
    /** Obtener el numero de Columnas en la matriz
     * @return 
     */
    public int getNumCols()
    {
        return ((ArrayList)matrix.get(0)).size();
    }
    
    /** Asigna determinado valor a una celda de la matriz
     * @param row basado en 0, es decir, si se necesita la fila i, entonces la posicion de esta es i-1 
     * @param col basado en 0, es decir, si se necesita la columna j, entonces la posicion de esta es j-1
     * @param val valor a asignar
     */
    public void setCellValue(int row, int col, Object val)
    {
       (this.getRow(row)).set(col, val);
    }
    
}
