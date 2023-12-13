package com.jelly.MightyMiner.utils.helper;

import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Wanted to use it in a real case nothing else thanks <3
public class MinHeap {
    public Node[] items;
    private int capacity;
    private int size = 0;

    public MinHeap(int capacity) {
        this.capacity = capacity;
        this.items = new Node[this.capacity];
    }

    public void add(Node node) {
        this.ensureCapacity();

        this.items[this.size] = node;
        this.size++;
        this.heapUp();
    }

    public BlockPos poll() {
        Node itemToPoll = this.items[0];
        this.items[0] = this.items[this.size - 1];
        this.size--;
        this.heapDown();
        return itemToPoll.block;
    }

    private void swap(int index1, int index2) {
        Node temp = this.items[index1];
        this.items[index1] = this.items[index2];
        this.items[index2] = temp;
    }

    private void ensureCapacity() {
        if (this.size == this.capacity) {
            this.capacity *= 2;
            this.items = Arrays.copyOf(this.items, this.capacity);
        }
    }

    private void heapUp() {
        int index = this.size - 1;
        while (index != 0 && this.items[this.parentIndex(index)].cost > this.items[index].cost) {
            this.swap(this.parentIndex(index), index);
            index = this.parentIndex(index);
        }
    }

    private void heapDown() {
        int index = 0;

        while (leftChildIndex(index) < this.size) {
            int smallChildIndex = leftChildIndex(index);
            int rightChildIndex = rightChildIndex(index);

            if (rightChildIndex < this.size && this.items[rightChildIndex].cost < this.items[smallChildIndex].cost) {
                smallChildIndex = rightChildIndex;
            }

            if (this.items[index].cost > this.items[smallChildIndex].cost) {
                swap(index, smallChildIndex);
                index = smallChildIndex;
            } else {
                break;
            }
        }
    }

    private int parentIndex(int childIndex) {
        return childIndex >>> 1;
    }

    private int leftChildIndex(int parentIndex) {
        return 2 * parentIndex + 1;
    }

    private int rightChildIndex(int parentIndex) {
        return 2 * parentIndex + 2;
    }

    public List<BlockPos> getBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        for (int i = 0; i < this.size; i++) {
            if(this.items[i] == null) break;
            blocks.add(this.items[i].block);
        }
        return blocks;
    }
}

