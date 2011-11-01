package show

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D

import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import javax.swing.JComponent

public class ShowComponent extends JComponent {

    List<TraySprite> traySprites = [].asSynchronized()
    Buffer upstream
    Buffer downstream
    List<ProcessingUnit> producers
    List<ProcessingUnit> consumers
    List<ProcessingUnit> units
    def producerLabelImage = ProcessingUnit.createLabelImage(160,80,"Producer")
    def consumerLabelImage = ProcessingUnit.createLabelImage(160,80,"Consumer")

    ShowComponent(params) {
        super()
        params.wip.times { traySprites << new TraySprite(this) }
        producers = (1 .. params.producers).collect { new ProcessingUnit(180, it * 100) }
        consumers = (1 .. params.consumers).collect { new ProcessingUnit(580, it * 100) }
        units = producers + consumers
        upstream   = new Buffer(x:  0, y:100, offset:50)
        downstream = new Buffer(x:400, y:100, offset:50)
    }


    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = g.create()
        
        drawTransformed(G2, producerLabelImage){ it.translate 180, 20 }
        drawTransformed(G2, consumerLabelImage){ it.translate 580, 20 }

        units.each { processingUnit ->
            drawTransformed(G2, processingUnit.image) { AffineTransform txf ->
                txf.translate processingUnit.x, processingUnit.y
            }
        }
        traySprites.each { sprite ->
            sprite.images.each { String name, BufferedImage image ->
                drawTransformed(G2, image) { AffineTransform txf ->
                    if (sprite.x > 400){
                        double frac = Math.min( 1d, (sprite.x - 400) / 206)
                        txf.translate sprite.x + frac*120, sprite.y + frac*50
                        txf.rotate(Math.toRadians( frac* 180  ))
                        return
                    }
                    txf.translate sprite.x, sprite.y
                }
            }
        }
        G2.dispose()
    }

    Dimension getMinimumSize() { [10,10] }

    private void drawTransformed(Graphics2D G2, BufferedImage image, Closure transform) {
        def oldTf = G2.transform
        AffineTransform txf = new AffineTransform(oldTf)

        transform txf

        G2.transform = txf
        G2.drawImage image, 0, 0, (ImageObserver) null
        G2.transform = oldTf
    }
}

