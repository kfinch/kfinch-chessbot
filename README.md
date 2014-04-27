kfinch-chessbot
===============

kfinch's chessbot! Contains a framework for running a game of chess, a frontend, and a bot.

USAGE:
  Currently the only UI option prints to the console and takes input from the console.
  I'm working on an actual GUI, but for now this will have to suffice.
  To access the frontend menu, launch the main method in chess_frontend/FrontendAscii.java

TODO:
High Priority -
  Implement a simple GUI to make actually playing with the chessbot easier / more convenient.
  Figure out a way to test the effectiveness of some of the harder to test features.
    (Like transposition tables, AB pruning, etc.)

Low Priority - 
  Fiddle with evaluator to optimize performance.
  Allow an option that adds more 'randomness' to bots moves.
  Enable server to detect and handle loops of repeated moves.
