# wakabaYes
This is a Java program which recognizes standard CAPTCHA used in Wakaba. Wakaba is an open source software that implements an image board (a kind of forum). It can be downloaded from http://wakaba.c3.cx/s/web/wakaba_kareha . In this image board, a user usually has to solve a CAPTCHA image challenge for every submitted post, in order to prevent spam. This program can do this automatically.

Image boards that use Wakaba with the default CAPTCHA include http://iichan.hk/ , http://www.wakachan.org/ and a number of less popular ones.

# How it works
First, the image is segmented using connected components and a number of hacks to split the characters which stick together. This is the weakest part of the program and it causes most errors.
Then each segmented character is fed to a LeNet5 (http://yann.lecun.com/exdb/lenet/ ) classifier. The classifier is pre-trained; I do not provide the code and the data to train it. It outputs a probability distribution over all letters for the given character image.

After that, the most probable word is derived from the known grammar which is used in Wakaba and the individual letter probabilities given by the classifier. This is done with some small approximations.

# Performance
On the default Wakaba settings for CAPTCHA, the precision is over 99.9%. If the letter distortion parameters are increased twofold, keeping character spacing the same, the precision is reduced to 98.5%. Average recognition time in batch mode is 0.05 seconds.
