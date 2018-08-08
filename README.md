# BitExplorer
Read the full PDF report above!

BitExplorer is a Bitcoin transaction visualizer created using Java for Bucknell's CSCI 205 Final Project. The project description was open-ended, though required us create a GUI and implement the software development principles we learned throughout the semester.

BitExplorer is written in Java and uses Processing's graphics package. It has two modes. The first mode, Transaction Stream, creates a bouncing ball visualization. Each new transaction is read through a websocket and parsed into a ball, with the size and color dependent on the transaction size. There are more features available for users to analyze the data further, such as selecting a ball to see the sender and receiver, or viewing a histogram of common transaction sizes.

The second mode, Blockchain Traveler, loads the most recent block and displays pertinent data, such as average transaction size, to the user. Then, the user can travel back to past blocks by fetching the parent block that is disclosed in each block's data.

# Deploy
Compile & run /BitcoinVisualizer/src/src/BitExplorer.java
# Project Report

Read Final Report.pdf
