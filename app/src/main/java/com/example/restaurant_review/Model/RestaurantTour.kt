package com.example.restaurant_review.Model

import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Log

class RestaurantTour : Parcelable{
    private var root: TourNode
    private var size = 0
    var tourGrid: Array<Array<TourNode?>> = Array(size) { Array(size) { null } }
    // x is rows (vertical), y is cols (horizontal)
    private lateinit var currPoint: Point


    constructor(parcel: Parcel) : this(parcel.readParcelable(TourNode::class.java.classLoader)!!, parcel.readInt()) {
        for (i in IntRange(0, size-1)){
            tourGrid[i] = parcel.createTypedArray(TourNode.CREATOR) as Array<TourNode?>
        }
    }

    constructor(root: TourNode, size: Int) {
        this@RestaurantTour.size = size
        this@RestaurantTour.root = root
        currPoint = Point(size/2, size/2)
        tourGrid = Array(size) { Array(size) { null } }
        tourGrid.put(currPoint, root)
    }

    // some utility extension fns
    fun Point.top (): Point { return Point(this.x-1, this.y) }
    fun Point.bottom (): Point { return Point(this.x+1, this.y) }
    fun Point.left (): Point { return Point(this.x, this.y-1) }
    fun Point.right (): Point { return Point(this.x, this.y+1) }
    fun Point.inBounds (): Boolean { return this.x in 0 until size && this.y in 0 until size }
    fun Array<Array<TourNode?>>.index (point: Point): TourNode?{
        return this[point.x][point.y]
    }
    fun Array<Array<TourNode?>>.put (point: Point, element: TourNode?){
        this[point.x][point.y] = element
    }
    fun Array<Array<TourNode?>>.find (tourNode: TourNode) : Point {
        for (i in IntRange(0, size-1)){
            for (j in IntRange(0, size-1)){
                if (tourGrid[i][j] == tourNode){
                    return Point(i, j)
                }
            }
        }
        throw ArrayIndexOutOfBoundsException()
    }
    val currNode get(): TourNode? {
        return tourGrid.index(currPoint)
    }


    fun hasTop () : Boolean { return currPoint.top().inBounds() && tourGrid.index(currPoint.top()) != null }
    fun hasLeft () : Boolean { return currPoint.left().inBounds() && tourGrid.index(currPoint.left()) != null }
    fun hasRight () : Boolean { return currPoint.right().inBounds() && tourGrid.index(currPoint.right()) != null }
    fun hasBottom () : Boolean { return currPoint.bottom().inBounds() && tourGrid.index(currPoint.bottom()) != null }

    fun goTop () { if (hasTop()) currPoint = currPoint.top() }
    fun goLeft () { if (hasLeft()) currPoint = currPoint.left() }
    fun goRight () { if (hasRight()) currPoint = currPoint.right() }
    fun goBottom () { if (hasBottom()) currPoint = currPoint.bottom() }

    fun addTop (fromNode: TourNode, inputNode: TourNode) {
        addTop(tourGrid.find(fromNode), inputNode)
    }
    fun addLeft (fromNode: TourNode, inputNode: TourNode) {
        addLeft(tourGrid.find(fromNode), inputNode)
    }
    fun addRight (fromNode: TourNode, inputNode: TourNode) {
        addRight(tourGrid.find(fromNode), inputNode)
    }
    fun addBottom (fromNode: TourNode, inputNode: TourNode) {
        addBottom(tourGrid.find(fromNode), inputNode)
    }

    fun addTop (point: Point, inputNode: TourNode) {
        tourGrid.put(point.top(), inputNode)
    }
    fun addLeft (point: Point, inputNode: TourNode) {
        tourGrid.put(point.left(), inputNode)
    }
    fun addRight (point: Point, inputNode: TourNode) {
        tourGrid.put(point.right(), inputNode)
    }
    fun addBottom (point: Point, inputNode: TourNode) {
        tourGrid.put(point.bottom(), inputNode)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeParcelable(root, flags)
        dest.writeInt(size)
        for (i in IntRange(0, size-1)) {
            dest.writeTypedArray(tourGrid[i], flags)
        }
    }

    companion object CREATOR : Parcelable.Creator<RestaurantTour> {
        override fun createFromParcel(parcel: Parcel): RestaurantTour {
            return RestaurantTour(parcel)
        }

        override fun newArray(size: Int): Array<RestaurantTour?> {
            return arrayOfNulls(size)
        }
    }
}

data class TourNode (var name: String, var image: Uri?): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readParcelable(Uri::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(image, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TourNode> {
        override fun createFromParcel(parcel: Parcel): TourNode {
            return TourNode(parcel)
        }

        override fun newArray(size: Int): Array<TourNode?> {
            return arrayOfNulls(size)
        }
    }
}