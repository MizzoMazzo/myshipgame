#set up build environment
FROM ubuntu:latest
COPY pom.xml /home/myshipgame/
COPY /src/ /home/myshipgame/src
COPY InsaneMap.json /home/myshipgame/
COPY settings.xml /usr/share/maven/ref/
WORKDIR /home/myshipgame

#install all tools and dependencies
RUN apt-get update && apt-get upgrade -y && apt-get install -y \
	maven \
	git \
	openjdk-11-jdk \
    	make \
    	unzip \
    	build-essential \
    	pkg-config \
    	libtool \
    	autoconf \
    	automake \
    	libzmq3-dev \
    	locate \
    	wget

#build the server
RUN mvn -s /usr/share/maven/ref/settings.xml clean package

WORKDIR /home

#install jzmq libraries and dependencies
RUN git clone https://github.com/zeromq/libzmq.git
RUN cd libzmq && ./autogen.sh && ./configure --prefix=/ && make && make install && ldconfig

RUN wget http://download.zeromq.org/zeromq-3.1.0-beta.zip
RUN unzip zeromq-3.1.0-beta.zip
RUN cd zeromq-3.1.0 && ./configure --prefix=/ && make && make install && ldconfig


RUN git clone https://github.com/zeromq/jzmq.git
RUN cd jzmq/jzmq-jni && ./autogen.sh && ./configure --prefix=/ && make && make install && ldconfig

EXPOSE 12345 12345

CMD java -jar /home/myshipgame/target/myshipgame-0.1.jar -port 12345 -seed 123 -timeout -1 -map /home/myshipgame/InsaneMap.json
