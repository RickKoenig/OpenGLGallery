package com.mperpetuo.openglgallery;

public class RunAvgLong {
	long[] data;
	int len = 0;
	int ptr = 0;
	long sum = 0L;
	public RunAvgLong(int size) {
		data = new long[size];
	}
	public long runavg(long in) {
		sum -= data[ptr];
		sum += in;
		data[ptr] = in;
		++len;
		if (len > data.length)
			len = data.length;
		++ptr;
		if (ptr >= data.length)
			ptr -= data.length;
		return (sum + (len>>1))/len;
	}
}
