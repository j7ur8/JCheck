package components;

import check.Overauth.Overauth;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import static burp.JCheckExtender.BurpAPI;


public class DiyJLogTable extends JTable {

    private static TableModel tableModel;
    private static Font font = null;

    private static TableColumnModel columnModel = null;
    public DiyJLogTable(TableModel model){
        super(model,new ColumnModelExample());

        tableModel = getModel();
        font = getFont();
        columnModel = getColumnModel();

        setAutoCreateColumnsFromModel(true);

        getSelectionModel().addListSelectionListener(this::listSelectionAction);
        setDefaultRenderer(Object.class, CellRendererExample());

    }


    private void listSelectionAction(ListSelectionEvent e){
        if(e.getValueIsAdjusting()){
            return;
        }

        switch (getName()){
            case "OverauthLoggerTable" -> {
                Overauth.setRequestResponse((String) this.getValueAt(this.getSelectedRow(),0));
            }
        }
    }


    private TableCellRenderer CellRendererExample(){
        return new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setEnabled(true);
                c.setForeground(Color.BLACK);
                if(column==4){
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.LEFT);
                }else if(column==0){
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.RIGHT);
                }else if(column==9 || column==10 || column==11) {
                    c.setForeground(Color.RED);
                }else{
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                }

                return c;
            }
        };
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }



    public static class ColumnModelExample extends DefaultTableColumnModel{

        private final FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);


        private void setWidth(TableColumn tableColumn, int width){
            tableColumn.setWidth(width);
            tableColumn.setPreferredWidth(width);
            tableColumn.setMaxWidth(width);
            tableColumn.setMinWidth(width);
        }

        @Override
        public TableColumn getColumn(int columnIndex) {

            TableColumn tableColumn = tableColumns.elementAt(columnIndex);

            System.out.println(tableColumn.getHeaderValue()+" "+tableColumn.getWidth()+" "+tableColumn.getPreferredWidth());
            switch (columnIndex){
                case 0:
                    int idWidth = (int) font.getStringBounds(String.valueOf(tableModel.getRowCount()), frc).getWidth();

                    setWidth(tableColumn, idWidth+10);
                    break;
                case 1,5:
                    setWidth(tableColumn, 50);
                    break;
                case 2,6,9,10,11:
                    setWidth(tableColumn, 70);
                    break;
                case 3,7,8:
                    setWidth(tableColumn, 160);
                    break;
            }


            return tableColumn;
        }
    }
    public static class TableModelExample extends AbstractTableModel{

        private final String[] Headers;

        private final Map<String,Map<String,String>> RowMap;

        private final Map<String, String> PathMap;

        public TableModelExample(String [] headers, Map<String, Map<String,String>> rowMap, Map<String, String> pathMap){
            Headers = headers;
            RowMap = rowMap;
            PathMap = pathMap;
        }

        @Override
        public int getRowCount() {
            return RowMap.size();
        }

        @Override
        public int getColumnCount() {
            return Headers.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try{
                String path = PathMap.get(String.valueOf(rowIndex));

                Map<String, String> logMap = RowMap.get(path);
                return logMap.get(Headers[columnIndex]);
            }catch (Exception e){
                BurpAPI.logging().logToError(e);
            }

            return "Error";
        }

        @Override
        public String getColumnName(int column){
            return Headers[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

    }


    public static void main(String[] args) {
        String[] headers = new String[]{"#","Tool","Method","Host","Path","Code","Length","RequestTime","ResponseTime","LowAuth","UnAuth"};
        Map<String, String> rowMap = new HashMap<>(){
            {
                put("#", "0");
                put("Tool", "Repeater");
                put("Method", "Post");
                put("Host", "baidu.com");
                put("Path", "/");
                put("Code", "");
                put("Length", "");
                put("RequestTime", "12.13");
                put("ResponseTime", "");
                put("LowAuth", "");
                put("UnAuth", "");

            }
        };


        Map<String,Map<String,String>> logMap = new HashMap<>();
        logMap.put("bac", rowMap);
        logMap.put("dac", rowMap);

        Map<String, String> pathMap = new HashMap<>(){
            {
                put("0", "bac");
                put("1", "dac");
            }
        };

        DiyJLogTable logTable = new DiyJLogTable(new TableModelExample( headers, logMap, pathMap));

        JFrame JrcetFrame = new JFrame("J7ur8's Remote Code Execute Tools");
        JPanel a = new JPanel(new GridBagLayout());
        a.setPreferredSize(new Dimension(1200,1000));

        a.add(new JScrollPane(logTable),new GridBagConstraints(
                0,0,
                1,1,
                1,0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0,0,0,0),
                0,0
        ));

        JrcetFrame.setContentPane(a);

        JrcetFrame.setResizable(true);
        JrcetFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JrcetFrame.setSize(1200, 1000);
        JrcetFrame.setVisible(true);

        rowMap.put("LowAuth","as");

    }

}
