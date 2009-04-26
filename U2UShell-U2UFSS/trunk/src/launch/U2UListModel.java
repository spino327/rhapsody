/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package launch;

import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.downloadpeer.U2UContentDiscoveryEvent;
import org.u2u.filesharing.downloadpeer.U2USearchListener;

/**
 *
 * @author Irene & Sergio
 */
public class U2UListModel implements ListModel, U2USearchListener{

    ArrayList<U2UContentAdvertisementImpl> list;
    private JList jList;

    public U2UListModel(JList l)
    {
        list = new ArrayList<U2UContentAdvertisementImpl>();
        jList = l;
    }

    public synchronized int getSize()
    {
        return list.size();
    }

    public Object getElementAt(int index)
    {
        if(list.size() > 0)
        {
            U2UContentAdvertisementImpl temp = list.get(index);

            return temp.getName()+" l = "+temp.getLength();
        }
        else
            return "---";   
    }

    public void addListDataListener(ListDataListener l)
    {
        
    }

    public void removeListDataListener(ListDataListener l)
    {
        
    }

    public void contentAdvertisementEvent(U2UContentDiscoveryEvent event)
    {
        if(event != null)
        {
            Enumeration<U2UContentAdvertisementImpl> en = event.getResponseAdv();

            while(en.hasMoreElements())
            {
                U2UContentAdvertisementImpl temp = en.nextElement();
                if(list.indexOf(temp) == -1)
                {
                    list.add(temp);
                }
            }

            jList.updateUI();
        }
        
    }

}
