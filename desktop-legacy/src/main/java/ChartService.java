import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartService {

    public static void showTransactionChart(List<Transaction> transactions) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Map<String, BigDecimal>> transactionSummary = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getType,
                        Collectors.groupingBy(
                                transaction -> transaction.getTimestamp().toLocalDate().toString(),
                                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                        )
                ));

        for (Map.Entry<String, Map<String, BigDecimal>> entry : transactionSummary.entrySet()) {
            String transactionType = entry.getKey();
            for (Map.Entry<String, BigDecimal> dateEntry : entry.getValue().entrySet()) {
                String date = dateEntry.getKey();
                BigDecimal amount = dateEntry.getValue();
                dataset.addValue(amount, transactionType, date);
            }
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Transaction History",
                "Date",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        JFrame frame = new JFrame();
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void showAccountBalancePieChart(List<Account> accounts) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Account account : accounts) {
            dataset.setValue(account.getAccountId(), account.getBalance());
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Account Balances",
                dataset,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        JFrame frame = new JFrame();
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void showTransactionTypeDistribution(List<Transaction> transactions) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        Map<String, Long> transactionTypeCount = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getType, Collectors.counting()));

        for (Map.Entry<String, Long> entry : transactionTypeCount.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Transaction Type Distribution",
                dataset,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        JFrame frame = new JFrame();
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
