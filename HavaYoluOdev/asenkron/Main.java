import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

class SeatClass {
    boolean isReserved;
    int seatNum;

    SeatClass(int seatNum) {
        this.seatNum = seatNum;
        this.isReserved = false;
    }
}

class FlightClass {
    List<SeatClass> seats = new ArrayList<>();
    private final JTextArea output;

    FlightClass(int numberOfSeats, JTextArea output) {
        this.output = output;
        for (int i = 1; i <= numberOfSeats; i++) {
            seats.add(new SeatClass(i));
        }
    }

    private void appendOutput(String message) {
        SwingUtilities.invokeLater(() -> {
            String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            output.append("Zaman: " + timeStamp + " " + message + "\n\n");
        });
    }

    public boolean makeReservation(int seatNum) {
        String requestMessage = "Koltuk numarası " + seatNum + " rezerve isteği";
        SeatClass seat = seats.get(seatNum - 1);
        String resultMessage;
        if (!seat.isReserved) {
            resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " başarıyla rezerve edildi.";
        } else {
            resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " önceden rezerve edildildiğinden rezerve edilemez.";
        }
        appendOutput(requestMessage + "\n" + resultMessage);
        seat.isReserved = true;
        return seat.isReserved;
    }

    public void queryReservation(int seatNum) {
        String requestMessage = "Koltuk numarası " + seatNum + " sorgulama isteği";
        String resultMessage = Thread.currentThread().getName() + " Boş koltuklar bakılıyor. Uygun koltuklar :\n" + getSeatStates();
        appendOutput(requestMessage + "\n" + resultMessage);
    }

    public boolean cancelReservation(int seatNum) {
        String requestMessage = "Koltuk numarası " + seatNum + " iptal isteği";
        SeatClass seat = seats.get(seatNum - 1);
        String resultMessage;
        if (seat.isReserved) {
            seat.isReserved = false;
            resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " rezervesi başarıyla iptal edildi.";
        } else {
            resultMessage = Thread.currentThread().getName() + " Koltuk numarası " + seatNum + " için rezervasyon iptal edilemedi çünkü bu koltuk rezerve edilmemişti.";
        }
        appendOutput(requestMessage + "\n" + resultMessage);
        return !seat.isReserved;
    }

    private String getSeatStates() {
        StringBuilder seatStates = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            seatStates.append("Koltuk No ").append(i + 1).append(" : ").append(seats.get(i).isReserved ? "1" : "0").append(" ");
        }
        return seatStates.toString();
    }
}

class ReaderThread extends Thread {
    FlightClass flight;

    ReaderThread(FlightClass flight) {
        this.flight = flight;
    }

    public void run() {
        flight.queryReservation(2);
        flight.queryReservation(8);
    }
}

class WriterThread extends Thread {
    FlightClass flight;

    WriterThread(FlightClass flight) {
        this.flight = flight;
    }

    public void run() {
        flight.makeReservation(6);
        flight.makeReservation(8);
        flight.makeReservation(2);
        flight.makeReservation(8);
        flight.cancelReservation(1);
        flight.cancelReservation(8);
    }
}

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Havayolu Rezervasyon Sistemi");
        JTextArea output = new JTextArea(50, 60);
        output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(output);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        FlightClass flight = new FlightClass(10, output);
        WriterThread writer1 = new WriterThread(flight);
        WriterThread writer2 = new WriterThread(flight);
        WriterThread writer3 = new WriterThread(flight);
        ReaderThread reader1 = new ReaderThread(flight);
        ReaderThread reader2 = new ReaderThread(flight);

        writer1.start();
        writer2.start();
        writer3.start();
        reader2.start();
        reader1.start();
    }
}
