<h1>Java Network File Sharing Demo</h1>

<p>This project demonstrates a simple Java application for network file sharing. It consists of two parts: a server and a client, each with its own graphical user interface (GUI).</p>

<h2>How to Run</h2>

<ol>
    <li>
        <strong>Server</strong>:
        <ul>
            <li>Run the <code>ServerGUI</code> class.</li>
            <li>Click the "Start" button to start the server.</li>
            <li>The server will start listening on port 5002.</li>
            <li>The "Connected Clients" label displays the number of currently connected clients.</li>
            <li>Logs will be displayed in the text area.</li>
        </ul>
    </li>
    <li>
        <strong>Client</strong>:
        <ul>
            <li>Run the <code>ClientGUI</code> class.</li>
            <li>The client will attempt to connect to the server at <code>localhost</code> on port 5002.</li>
            <li>The client GUI allows for file upload, download, and deletion. The log panel displays activities.</li>
        </ul>
    </li>
</ol>

<p><strong>Note</strong>: Ensure that the server is running before starting any client.</p>
<h2>Demo - Part 1</h2>
<img src="https://i.postimg.cc/d3Vzrwr6/1.gif" alt="Demonstration part 1">

<h2>Demo - Part 2</h2>
<img src="https://i.postimg.cc/3NLV8Vyq/2.gif" alt="Demonstration part 2">

<h2>Project Structure</h2>

<ul>
    <li><code>ClientGUI</code>: This class handles the client's graphical user interface. It provides functionalities for uploading, downloading, and deleting files from the server.</li>
    <li><code>ClientHandler</code>: This class facilitates communication between the server and individual clients. It employs threads to concurrently handle multiple client connections. The class is responsible for processing messages received from clients and executing file-related operations.</li>
    <li><code>JServer</code>: This class represents the server. It manages client connections, handles file operations, and provides methods for starting and stopping the server.</li>
    <li><code>ServerGUI</code>: This class is the graphical user interface for the server. It allows the user to start and stop the server, view logs, and see the number of connected clients.</li>
    <li><code>DatabaseHandler</code>: This class manages the SQLite database used for storing file data.</li>
</ul>

<h2>Dependencies</h2>

<p>This project uses Java's built-in libraries for socket programming, GUI, and file handling. And the sqlite-jdbc-3.43.0.0 driver to connect with database</p>

<h2>Database</h2>

<p>An SQLite database is used to store file data. The <code>DatabaseHandler</code> class handles database operations.</p>

<h2>Usage</h2>

<p>This project serves as a simple demonstration of how Java can be used in network programming for data sharing. It allows clients to upload, download, and delete files on the server.</p>

<h2>Note</h2>

<p>This project was created using Visual Studio Code with the Extension Pack for Java.<br>
Make sure to have Java installed on your system.</p>

<p><strong>Disclaimer</strong>: This project is a basic demonstration and may not be suitable for production use without further development and security considerations.</p>

<h2>License</h2>

<p><a href="LICENSE">MIT License</a></p>

<p>Feel free to reach out if you have any questions or need further assistance. Happy coding!</p>
