/*
 * PruebaTabla.fx
 *
 * Created on 21-may-2009, 20:58:04
 */

package org.u2u.gui.scene;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javafx.ext.swing.SwingComponent;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.*;

/**
 * @author Irene
 */
//class SwingTable extends SwingComponent{
public class SwingTable extends SwingComponent{

    var table: JTable;
    var model: DefaultTableModel;
    public var selection: Integer;

    public var columns: TableColumn[] on replace{
        model = new DefaultTableModel(for(column in columns) column.text, 0);
        table.setModel(model);
    };

    public var rows: TableRow[] on replace oldValue[lo..hi] = newVals{
        for(index in [hi..lo step -1]){
            model.removeRow(index);
        }

        for(row in newVals){
            model.addRow(for(cell in row.cells) cell.text);
        }
    };

    public override function createJComponent(){
        table = new JTable();
        model =
        table.getModel() as DefaultTableModel;
   
        var selectionModel = table.getSelectionModel();
        selectionModel.addListSelectionListener(
        ListSelectionListener{
            public override function valueChanged(e: ListSelectionEvent ) {
                selection = table.getSelectedRow();
            }
        }
        );
        return new JScrollPane(table);
    }

}

//class TableColumn{
package class TableColumn{
    public var text: String;
}
//class TableCell{
package class TableCell{
    public var text: String;
}

//class TableRow{
package class TableRow{
    public var cells: TableCell[];
}

